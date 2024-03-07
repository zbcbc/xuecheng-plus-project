package com.xuecheng.content.model.dto;

import lombok.Data;
import lombok.ToString;

/**
 * @author 85452
 * @Create: 2024/2/6 - 16:15
 * 课程查询条件模型
 */
@Data
@ToString
public class QueryCourseParamsDto {
    //审核状态
    private String auditStatus;

    //课程名称
    private String courseName;

    //发布状态
    private String publishStatus;
}
