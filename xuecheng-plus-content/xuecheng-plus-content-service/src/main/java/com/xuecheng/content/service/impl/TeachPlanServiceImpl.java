package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachPlanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ClassName: TeachPlanServiceImpl
 * Package: com.xuecheng.content.service.impl
 * Description:
 *
 * @Author zbc
 * @Create 2024/2/28 14:58
 * @Version 1.0
 */
@Service
public class TeachPlanServiceImpl implements TeachPlanService {
    @Autowired
    TeachplanMapper teachplanMapper;
    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;
    @Override
    public List<TeachPlanDto> findTeachplanTree(long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }

    @Override
    @Transactional
    public void saveTeachPlan(SaveTeachplanDto saveTeachplanDto) {
        //通过课程计划id判断是新增还是修改
        Long teachplanId = saveTeachplanDto.getId();
        if(teachplanId == null){
            //新增
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);

            //确定排序字段，找到同级结点个数，排序字段=个数+1
            //select count(1) from teachplan where parentid = ? and course_id = ?
            Long courseId = saveTeachplanDto.getCourseId();
            Long parentid = saveTeachplanDto.getParentid();
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper = queryWrapper.eq(Teachplan::getCourseId, courseId)
                    .eq(Teachplan::getParentid, parentid);
            Integer count = teachplanMapper.selectCount(queryWrapper);
            teachplan.setOrderby(count + 1);

            teachplanMapper.insert(teachplan);
        }else{
            //修改
            Teachplan teachplan = teachplanMapper.selectById(teachplanId);
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);
            teachplanMapper.updateById(teachplan);
        }
    }

    @Transactional
    @Override
    public void deleteTeachPlan(Long teachplanId) {
        if(teachplanId == null){
            XueChengPlusException.cast("该课程计划id为空");
        }
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getParentid, teachplanId);
        Integer count = teachplanMapper.selectCount(queryWrapper);

        if(count > 0){
            XueChengPlusException.cast("课程计划信息还有子级信息，无法操作");
        }else{
            LambdaQueryWrapper<TeachplanMedia> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(TeachplanMedia::getTeachplanId, teachplanId);
            teachplanMediaMapper.delete(queryWrapper1);
            teachplanMapper.deleteById(teachplanId);
        }
    }

    @Override
    @Transactional
    public void moveTeachPlan(String moveType, Long teachplanId) {
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        //大章节查同一课程下的章节 小章节查同一父章节下的
        Long courseId = teachplan.getCourseId();
        Long parentid = teachplan.getParentid();
        Integer grade = teachplan.getGrade();//课程等级
        Integer orderby = teachplan.getOrderby();

        //上移
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<Teachplan>();
        if("moveup".equals(moveType)){

            queryWrapper.lt(Teachplan::getOrderby, orderby)
                    .eq(Teachplan::getGrade, grade)
                    .orderByDesc(Teachplan::getOrderby)
                    .last("LIMIT 1");

            if(grade == 1){
                queryWrapper.eq(Teachplan::getCourseId, courseId);
            } else if (grade == 2) {
                queryWrapper.eq(Teachplan::getParentid, parentid);
            }
        }else if("movedown".equals(moveType)){
            queryWrapper.gt(Teachplan::getOrderby, orderby)
                    .eq(Teachplan::getGrade, grade)
                    .orderByAsc(Teachplan::getOrderby)
                    .last("LIMIT 1");
            if(grade == 1){
                queryWrapper.eq(Teachplan::getCourseId, courseId);
            } else if (grade == 2) {
                queryWrapper.eq(Teachplan::getParentid, parentid);
            }
        }
        //查询上一个plan
        Teachplan tmp = teachplanMapper.selectOne(queryWrapper);
        //交换
        exchangeTeachplanOrderBy(teachplan, tmp);
    }

    /**
     * 教学计划媒资绑定
     * @param bindTeachplanMediaDto
     */
    @Override
    @Transactional
    public TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {
        Long teachplanId = bindTeachplanMediaDto.getTeachplanId();
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if(teachplan == null){
            XueChengPlusException.cast("教学计划不存在");
        }
        Integer grade = teachplan.getGrade();
        if(grade != 2){
            XueChengPlusException.cast("只允许第二级教学计划绑定媒资文件");
        }
        Long courseId = teachplan.getCourseId();

        //先删除原有记录，再添加原有记录
        //根据课程计划id 删除它所绑定的媒资信息
        int delete = teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getTeachplanId, teachplanId));
        //添加原有记录
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        BeanUtils.copyProperties(bindTeachplanMediaDto, teachplanMedia);
        teachplanMedia.setCourseId(courseId);
        teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
        teachplanMediaMapper.insert(teachplanMedia);
        return teachplanMedia;
    }

    @Override
    public void unassociationMedia(Long teachPlanId, String mediaId) {
        LambdaQueryWrapper<TeachplanMedia> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeachplanMedia::getTeachplanId, teachPlanId)
                        .eq(TeachplanMedia::getMediaId, mediaId);
        teachplanMediaMapper.delete(wrapper);
    }

    public void exchangeTeachplanOrderBy(Teachplan teachplan1, Teachplan tmp){
        if(tmp == null){
            XueChengPlusException.cast("无法再移动了");
        }else{
            Integer orderby = teachplan1.getOrderby();
            Integer orderbyTmp = tmp.getOrderby();
            teachplan1.setOrderby(orderbyTmp);
            tmp.setOrderby(orderby);
            teachplanMapper.updateById(teachplan1);
            teachplanMapper.updateById(tmp);
        }
    }

}
