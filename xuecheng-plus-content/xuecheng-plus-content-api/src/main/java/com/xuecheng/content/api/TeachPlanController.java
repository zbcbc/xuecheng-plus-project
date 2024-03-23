package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.service.TeachPlanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ClassName: TeachPlanController
 * Package: com.xuecheng.content.api
 * Description:
 *
 * @Author zbc
 * @Create 2024/2/26 16:52
 * @Version 1.0
 */
@Api(value = "课程计划管理接口", tags = "课程计划管理接口")
@RestController
public class TeachPlanController {
    @Autowired
    TeachPlanService teachPlanService;
    @ApiOperation("查询课程计划")
    @ApiImplicitParam(value = "courseId",name = "课程基础Id值",required = true,dataType = "Long",paramType = "path")
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachPlanDto> getTreeNodes(@PathVariable Long courseId){
        return teachPlanService.findTeachplanTree(courseId);
    }

    @ApiOperation("课程计划创建/修改")
    @PostMapping("/teachplan")
    public void saveTeachPlan(@RequestBody SaveTeachplanDto saveTeachplanDto){
        teachPlanService.saveTeachPlan(saveTeachplanDto);
    }

    @ApiOperation("删除课程计划")
    @DeleteMapping("/teachplan/{teachplanId}")
    public void deleteTeachPlan(@PathVariable Long teachplanId){
        teachPlanService.deleteTeachPlan(teachplanId);
    }

    @ApiOperation("课程计划排序")
    @PostMapping("/teachplan/{moveType}/{teachplanId}")
    public void moveTeachPlan(@PathVariable String moveType, @PathVariable Long teachplanId){
        teachPlanService.moveTeachPlan(moveType, teachplanId);
    }

    @ApiOperation("绑定课程计划和媒资信息")
    @PostMapping("/teachplan/association/meida")
    public void associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto){
        teachPlanService.associationMedia(bindTeachplanMediaDto);
    }

    @ApiOperation("解除课程计划和媒资信息的绑定信息")
    @DeleteMapping("/teachplan/association/media/{teachPlanId}/{mediaId}")
    public void unassociationMedia(@PathVariable Long teachPlanId, @PathVariable String mediaId){
        teachPlanService.unassociationMedia(teachPlanId, mediaId);
    }

}
