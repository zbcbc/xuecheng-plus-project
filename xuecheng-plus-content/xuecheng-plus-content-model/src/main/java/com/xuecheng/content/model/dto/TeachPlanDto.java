package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * ClassName: TeachPlanDto
 * Package: com.xuecheng.content.model.dto
 * Description: 课程计划的dto
 *
 * @Author zbc
 * @Create 2024/2/26 16:47
 * @Version 1.0
 */
@Data
@ToString
public class TeachPlanDto extends Teachplan {
    //课程计划相关联的媒资信息
    TeachplanMedia teachplanMedia;

    //子结点
    List<TeachPlanDto> teachPlanTreeNodes;

}
