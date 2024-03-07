package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * ClassName: EditCourseDto
 * Package: com.xuecheng.content.model.dto
 * Description:
 *
 * @Author zbc
 * @Create 2024/2/24 16:44
 * @Version 1.0
 */
@Data
@ApiModel(value = "EditCourseDto", description = "修改课程dto")
public class EditCourseDto extends AddCourseDto{
    @ApiModelProperty(value = "课程id", required = true)
    private Long id;
}
