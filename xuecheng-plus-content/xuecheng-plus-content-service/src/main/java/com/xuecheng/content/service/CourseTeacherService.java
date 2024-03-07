package com.xuecheng.content.service;

import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

/**
 * ClassName: CourseTeacherService
 * Package: com.xuecheng.content.service
 * Description:
 *
 * @Author zbc
 * @Create 2024/2/29 16:48
 * @Version 1.0
 */
public interface CourseTeacherService {
    List<CourseTeacher> queryTeacher(Long courseId);

    List<CourseTeacher> insertOrUpdateTeacher(CourseTeacher courseTeacher);

    void deleteTeacher(Long courseId, Long teacherId);
}
