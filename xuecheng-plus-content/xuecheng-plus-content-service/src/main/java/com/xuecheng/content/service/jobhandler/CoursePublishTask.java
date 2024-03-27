package com.xuecheng.content.service.jobhandler;

import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ClassName: CoursePublishTask
 * Package: com.xuecheng.content.service.jobhandler
 * Description:
 *
 * @Author zbc
 * @Create 2024/3/27 19:04
 * @Version 1.0
 */
@Component
@Slf4j
public class CoursePublishTask extends MessageProcessAbstract {


    /**
     * 任务调度入口
     */
    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        //调用抽象类的方法执行任务
        this.process(shardIndex, shardTotal, "course_publish", 30, 60);

    }

    /**
     * 执行课程发布的逻辑
     * @param mqMessage 执行任务内容
     * @return
     */
    @Override
    public boolean execute(MqMessage mqMessage) {
        //从mqmessage拿课程id
        Long courseId = Long.parseLong(mqMessage.getBusinessKey1());

        //课程静态化上传至minio
        generateCourseHtml(mqMessage, courseId);

        //向elasticsearch写索引

        //向redis写缓存

        //返回true表示任务完成
        return true;
    }

    /**
     * 生成静态化页面并上传至minio
     * @param mqMessage
     * @param courseId
     */
    private void generateCourseHtml(MqMessage mqMessage, Long courseId){
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        //任务幂等性处理
        //查询数据库取出该阶段的执行状态
        int stageOne = mqMessageService.getStageOne(taskId);
        if(stageOne > 0){
            log.debug("课程静态化任务完成，无需处理");
            return;
        }

        //todo:开始课程静态化


        //任务处理完成，更新任务状态
        mqMessageService.completedStageOne(taskId);
    }

    /**
     * 保存课程索引信息
     */

    public void saveCourseIndex(MqMessage mqMessage,long courseId){
        log.debug("保存课程索引信息,课程id:{}",courseId);
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        //任务幂等性处理
        //查询数据库取出该阶段的执行状态
        int stageTwo = mqMessageService.getStageTwo(taskId);
        if(stageTwo > 0){
            log.debug("写入课程索引任务完成，无需处理");
            return;
        }

        //todo:查询课程信息，调用搜索服务添加索引


        //任务处理完成，更新任务状态
        mqMessageService.completedStageTwo(taskId);

    }

    //将课程信息缓存至redis
    public void saveCourseCache(MqMessage mqMessage,long courseId){
        log.debug("将课程信息缓存至redis,课程id:{}",courseId);
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();

        //任务幂等性处理
        //查询数据库取出该阶段的执行状态
        int stageThree = mqMessageService.getStageThree(taskId);
        if(stageThree > 0){
            log.debug("写入课程索引任务完成，无需处理");
            return;
        }

        //todo:查询课程信息，调用搜索服务添加索引


        //任务处理完成，更新任务状态
        mqMessageService.completedStageThree(taskId);
    }

}
