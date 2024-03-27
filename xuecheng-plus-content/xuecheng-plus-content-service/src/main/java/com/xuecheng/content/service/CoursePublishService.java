package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;

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
}
