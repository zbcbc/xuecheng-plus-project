package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.po.CoursePublish;

import java.io.File;

/**
 * ClassName: CoursePublishService
 * Package: com.xuecheng.content.service
 * Description: 课程发布相关接口
 *
 * @Author zbc
 * @Create 2024/3/25 14:22
 * @Version 1.0
 */
public interface CoursePublishService {
    /**
     * 获取课程预览信息
     * @param courseId
     * @return
     */
    public CoursePreviewDto getCoursePreviewInfo(Long courseId);

    /**
     * 提交审核
     * @param companyId
     * @param courseId
     */
    public void commitAudit(Long companyId, Long courseId);

    /**
     * 课程发布
     * @param companyId
     * @param courseId
     */
    public void publish(Long companyId, Long courseId);

    /**
     * 课程静态化
     * @param courseId
     * @return
     */
    public File generateCourseHtml(Long courseId);

    /**
     * 上传课程静态化页面
     * @param courseId
     * @param file
     */
    public void uploadCourseHtml(Long courseId, File file);

    public CoursePublish getCoursePublish(Long courseId);


}
