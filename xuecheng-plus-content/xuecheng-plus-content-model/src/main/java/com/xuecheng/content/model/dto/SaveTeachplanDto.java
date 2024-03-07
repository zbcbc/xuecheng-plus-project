package com.xuecheng.content.model.dto;

import lombok.Data;
import lombok.ToString;

/**
 * ClassName: SaveTeachplanDto
 * Package: com.xuecheng.content.model.dto
 * Description: 新增大章节、小章节、修改课程信息
 *
 * @Author zbc
 * @Create 2024/2/28 15:20
 * @Version 1.0
 */
@Data
@ToString
public class SaveTeachplanDto {
    private Long id;
    private String pname;
    private Long parentid;
    private int grade;
    private String mediaType;
    private Long courseId;
    /**
     * 课程发布标识
     */
    private Long coursePubId;


    /**
     * 是否支持试学或预览（试看）
     */
    private String isPreview;

}
