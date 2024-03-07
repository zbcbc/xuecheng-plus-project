package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;

import java.util.List;

/**
 * ClassName: TeachPlanService
 * Package: com.xuecheng.content.service
 * Description:
 *
 * @Author zbc
 * @Create 2024/2/28 14:57
 * @Version 1.0
 */

public interface TeachPlanService {
    public List<TeachPlanDto> findTeachplanTree(long courseId);

    public void saveTeachPlan(SaveTeachplanDto saveTeachplanDto);

    void deleteTeachPlan(Long teachplanId);

    void moveTeachPlan(String moveType, Long teachplanId);
}
