package com.xuecheng.media.service;

import com.xuecheng.media.model.po.MediaProcess;

import java.util.List;

/**
 * ClassName: MediaFileProcessService
 * Package: com.xuecheng.media.service
 * Description:
 *
 * @Author zbc
 * @Create 2024/3/19 14:21
 * @Version 1.0
 */
public interface MediaFileProcessService {
    /**
     * @param shardTotal 分片总数
     * @param shardIndex 分片序号
     * @param count 获取记录数
     * @return
     */
    List<MediaProcess> selectListByShardIndex(int shardTotal, int shardIndex, int count);

    /**
     *  开启一个任务
     * @param id 任务id
     * @return true开启任务成功，false开启任务失败
     */
    public boolean startTask(long id);

    /**
     * @description 保存任务结果
     * @param taskId  任务id
     * @param status 任务状态
     * @param fileId  文件id
     * @param url url
     * @param errorMsg 错误信息
     * @return void
     * @author Mr.M
     * @date 2022/10/15 11:29
     */
    public void saveProcessFinishStatus(Long taskId,String status,String fileId,String url,String errorMsg);


}
