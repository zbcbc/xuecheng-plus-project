package com.xuecheng.media.service.jobHandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * XxlJob开发示例（Bean模式）
 *
 * 开发步骤：
 *      1、任务开发：在Spring Bean实例中，开发Job方法；
 *      2、注解配置：为Job方法添加注解 "@XxlJob(value="自定义jobhandler名称", init = "JobHandler初始化方法", destroy = "JobHandler销毁方法")"，注解value值对应的是调度中心新建任务的JobHandler属性的值。
 *      3、执行日志：需要通过 "XxlJobHelper.log" 打印执行日志；
 *      4、任务结果：默认任务结果为 "成功" 状态，不需要主动设置；如有诉求，比如设置任务结果为失败，可以通过 "XxlJobHelper.handleFail/handleSuccess" 自主设置任务结果；
 *
 * @author xuxueli 2019-12-11 21:52:51
 */
@Component
@Slf4j
public class VideoTask {
    @Autowired
    MediaFileProcessService mediaFileProcessService;

    @Autowired
    MediaFileService mediaFileService;

    @Value("${videoprocess.ffmpegpath}")
    private String ffmpegpath;


    /**
     * 分片广播任务 -- 视频处理任务
     */
    @XxlJob("videoJobHandler")
    public void shardingJobHandler() throws Exception {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        //确定cpu的核心数 最多就并行处理这么多任务
        int processors = Runtime.getRuntime().availableProcessors();

        //###查询当前执行器的待处理任务
        List<MediaProcess> mediaProcessList = mediaFileProcessService.selectListByShardIndex(shardTotal, shardIndex, processors);

        //任务数量
        int size = mediaProcessList.size();
        log.debug("取到视频处理任务数:" + size);
        if(size <= 0){
            return;
        }

        //创建一个size大小的线程池
        ExecutorService executorService = Executors.newFixedThreadPool(size);
        //##计数器##
        CountDownLatch countDownLatch = new CountDownLatch(size);

        mediaProcessList.forEach(mediaProcess -> {
            //将任务加入线程池 executorService.execute()是启动多个线程 而()中是线程内的处理逻辑 所以整个shardingJobHandler()方法瞬间就结束了 线程内的处理并不在这里进行
            //而我们需要 list中的所有任务都执行完 该方法才结束 这样任务执行完 下次能再调度该执行器执行其他任务
            //所以使用一个计数器countDownLatch 阻塞 并且减完(使用try-finally 避免有异常 不减1) 任务才结束

            executorService.execute(() -> {
                try {
                    //#####任务执行逻辑:
                    //1.#乐观锁争抢任务
                    Long taskId = mediaProcess.getId();
                    boolean b = mediaFileProcessService.startTask(taskId); //是否抢到锁
                    if (!b) {
                        log.debug("抢占任务失败,任务id:{}", taskId);
                        return; //使用lambda表达式实现Runnable，这里return相当于结束run()方法的执行 线程会回到线程池中 其他等待执行的任务不会受到影响
                    }

                    //2.#抢到锁->执行视频转码
                    //下载minIO视频到本地
                    String bucket = mediaProcess.getBucket();
                    String objectName = mediaProcess.getFilePath();
                    File file = mediaFileService.downloadFileFromMinIO(bucket, objectName);
                    //文件id就是md5
                    String fileId = mediaProcess.getFileId();
                    if (file == null) {
                        log.debug("下载视频出错,任务id:{}, bucket:{}, objectName:{}", taskId, bucket, objectName);
                        //保存任务处理失败的结果
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "下载视频到本地失败");
                        return;
                    }

                    //## 准备工具类对象的参数
                    //源avi视频的路径
                    String video_path = file.getAbsolutePath();
                    //转换后mp4文件的名称
                    String mp4_name = fileId + ".mp4";

                    //转换后mp4文件的路径 -> 先创建一个临时文件，作为转换后的文件路径
                    File mp4File = null;
                    try {
                        mp4File = File.createTempFile("minio", ".mp4");
                    } catch (IOException e) {
                        log.debug("创建临时文件异常,{}", e.getMessage());
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "创建临时文件异常");
                        return;
                    }
                    String mp4_path = mp4File.getAbsolutePath();

                    //## 创建工具类对象
                    Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegpath, video_path, mp4_name, mp4_path);

                    //## 开始视频转换，成功将返回success
                    String result = videoUtil.generateMp4();
                    if (!result.equals("success")) {
                        log.debug("视频转码出错,原因:{}, bucket:{}, objectName:{}", result, bucket, objectName);
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, result);
                        return;
                    }

                    //3.上传至minIO
                    boolean b1 = mediaFileService.addMediaFilesToMinIO(mp4File.getAbsolutePath(), bucket, objectName, "video/mp4");
                    if (!b1) {
                        log.debug("上传MP4至minio出错,taskid:{}", taskId);
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "上传MP4至minio出错");
                        return;
                    }

                    //4.保存任务的处理结果
                    //mp4文件的url
                    String url = getFilePathByMd5(fileId, ".mp4");
                    //更新任务状态为成功
                    mediaFileProcessService.saveProcessFinishStatus(taskId, "2", fileId, url, null);
                }finally {
                    countDownLatch.countDown(); //计数器减1
                }
            });//线程中的执行逻辑
        });//foreach

        //阻塞 size个线程都阻塞在这里，都完成后才结束当前方法
        //指定一个最大限度的等待时间，一但出现如断电的问题 导致计数器无法归0，让阻塞可以释放
        countDownLatch.await(30, TimeUnit.MINUTES);

    }


    private String getFilePathByMd5(String fileMd5, String fileExt){
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5 +fileExt;
    }



}
