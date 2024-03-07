package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ClassName: CourseTeacherServiceImpl
 * Package: com.xuecheng.content.service.impl
 * Description:
 *
 * @Author zbc
 * @Create 2024/2/29 16:48
 * @Version 1.0
 */
@Service
@Slf4j
public class CourseTeacherServiceImpl implements CourseTeacherService {

    @Autowired
    CourseTeacherMapper courseTeacherMapper;

    @Override
    public List<CourseTeacher> queryTeacher(Long courseId) {

        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseId);
        return courseTeacherMapper.selectList(queryWrapper);
    }

    @Transactional
    @Override
    public List<CourseTeacher> insertOrUpdateTeacher(CourseTeacher courseTeacher) {
        Long teacherId = courseTeacher.getId();
        //id为空 新增
        if(teacherId == null){
            CourseTeacher teacher = new CourseTeacher();
            BeanUtils.copyProperties(courseTeacher, teacher);
            teacher.setCreateDate(LocalDateTime.now());
            int insert = courseTeacherMapper.insert(teacher);
            if (insert <= 0)
                XueChengPlusException.cast("新增失败");
            return queryTeacher(teacher.getCourseId());
        }else{
            //更新
            CourseTeacher teacher = courseTeacherMapper.selectById(teacherId);
            BeanUtils.copyProperties(courseTeacher, teacher);
            int flag = courseTeacherMapper.updateById(teacher);
            if (flag <= 0)
                XueChengPlusException.cast("修改失败");
            return queryTeacher(teacher.getCourseId());
        }
    }

    @Override
    public void deleteTeacher(Long courseId, Long teacherId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getId, teacherId);
        queryWrapper.eq(CourseTeacher::getCourseId, courseId);
        int flag = courseTeacherMapper.delete(queryWrapper);
        if(flag <= 0){
             XueChengPlusException.cast("删除失败");
        }
    }
}
