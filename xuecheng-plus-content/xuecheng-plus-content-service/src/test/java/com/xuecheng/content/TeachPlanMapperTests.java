package com.xuecheng.content;

import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.service.TeachPlanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * ClassName: TeachPlanMapperTests
 * Package: com.xuecheng.content
 * Description:
 *
 * @Author zbc
 * @Create 2024/2/28 14:24
 * @Version 1.0
 */
@SpringBootTest
public class TeachPlanMapperTests {
    @Autowired
    TeachplanMapper teachplanMapper;
    @Autowired
    TeachPlanService teachPlanService;
    @Test
    public void testSelectTreeNodes(){
        SaveTeachplanDto saveTeachplanDto = new SaveTeachplanDto();
        saveTeachplanDto.setCourseId(117L);
        saveTeachplanDto.setParentid(0L);
        saveTeachplanDto.setPname("0.0");
        teachPlanService.saveTeachPlan(saveTeachplanDto);

        List<TeachPlanDto> teachPlanDtos = teachplanMapper.selectTreeNodes(117L);
        System.out.println(teachPlanDtos);
    }
}
