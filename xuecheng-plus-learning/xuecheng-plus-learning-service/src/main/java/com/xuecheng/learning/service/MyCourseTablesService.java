package com.xuecheng.learning.service;

import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.model.po.XcCourseTables;

/**
 * ClassName: MyCourseTablesService
 * Package: com.xuecheng.learning.service
 * Description:
 *
 * @Author zbc
 * @Create 2024/4/9 18:58
 * @Version 1.0
 */
public interface MyCourseTablesService {
    /**
     * 添加选课
     * @param userId
     * @param courseId
     * @return
     */
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId);

    public XcChooseCourse addFreeCourse(String userId, CoursePublish coursepublish);

    public XcCourseTables addCourseTables(XcChooseCourse chooseCourse);
    public XcChooseCourse addChargeCourse(String userId,CoursePublish coursepublish);

    public XcCourseTablesDto getLearningStatus(String userId, Long courseId);
}
