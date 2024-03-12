package com.xuecheng.content.api;

import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ClassName: CourseTeacherController
 * Package: com.xuecheng.content.api
 * Description:
 *
 * @Author zbc
 * @Create 2024/2/29 16:43
 * @Version 1.0
 */
@Api(value = "师资管理接口", tags = "师资管理接口")
@RestController
public class CourseTeacherController {
    @Autowired
    CourseTeacherService courseTeacherService;
    @ApiOperation(value = "查询教师信息接口")
    @GetMapping("/courseTeacher/list/{courseId}")
    public List<CourseTeacher> queryTeacher(@PathVariable Long courseId){
        return courseTeacherService.queryTeacher(courseId);
    }

    @ApiOperation(value = "新增/修改教师接口")
    @PostMapping("/courseTeacher")
    public List<CourseTeacher> insertOrUpdateTeacher(@RequestBody CourseTeacher courseTeacher){
        return courseTeacherService.insertOrUpdateTeacher(courseTeacher);
    }

    @ApiOperation(value = "删除教师接口")
    @DeleteMapping("/courseTeacher/course/{courseId}/{teacherId}")
    public void deleteTeacher(@PathVariable Long courseId, @PathVariable Long teacherId){
         courseTeacherService.deleteTeacher(courseId, teacherId);
    }
}
