package com.xuecheng.content.model.dto;

import lombok.Data;

import java.util.List;

/**
 * ClassName: CoursePreviewDto
 * Package: com.xuecheng.content.model.dto
 * Description: 用于课程预览的模型类
 *
 * @Author zbc
 * @Create 2024/3/25 14:19
 * @Version 1.0
 */
@Data
public class CoursePreviewDto {
    //课程基本信息 营销信息
    private CourseBaseInfoDto courseBase;
    //计划信息
    private List<TeachPlanDto> teachPlans;
    //师资信息 ...
}
