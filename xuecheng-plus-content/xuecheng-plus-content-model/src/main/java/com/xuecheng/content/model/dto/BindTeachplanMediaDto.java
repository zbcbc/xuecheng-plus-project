package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * ClassName: BindTeachplanMediaDto
 * Package: com.xuecheng.content.model.dto
 * Description: 绑定媒资和课程计划的模型类
 *
 * @Author zbc
 * @Create 2024/3/22 16:39
 * @Version 1.0
 */
@Data
public class BindTeachplanMediaDto {
    @ApiModelProperty(value = "媒资文件id", required = true)
    private String mediaId;

    @ApiModelProperty(value = "媒资文件名称", required = true)
    private String fileName;

    @ApiModelProperty(value = "课程计划标识", required = true)
    private Long teachplanId;

}
