package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Create: 2024/2/17 - 17:23
 */
@Service
@Slf4j
public class CourseCategoryServiceImpl implements CourseCategoryService {
    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        //调用mapper 递归查询出分类信息
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes(id);

        //找到每个节点的子节点，最终封装List<CourseCategoryTreeDto>
        //先将List转成map，key为节点id，value为dto对象，目的就是为了方便从map获取节点

        Map<String, CourseCategoryTreeDto> mapTemp = courseCategoryTreeDtos.stream()
                .filter(item -> !id.equals(item.getId()))
                .collect(Collectors.toMap(key -> key.getId(), value -> value, (key1, key2) -> key2));

        List<CourseCategoryTreeDto> resultList = new ArrayList<>();

        //从头遍历，一边遍历一边找子节点放到父节点的childrenTreeNodes
        courseCategoryTreeDtos.stream()
                //根节点不放 过滤掉根节点
                .filter(item -> !id.equals(item.getId()))
                .forEach(item -> {
                    //向list写入元素
                    if(item.getParentid().equals(id)){
                        resultList.add(item);
                    }

                    //找到每个节点的子节点 放在父节点的childerTreeNodes属性中
                    //从map中找到父节点
                    CourseCategoryTreeDto parent = mapTemp.get(item.getParentid());
                    //找到了再执行
                    if(parent != null){
                        if(parent.getChildrenTreeNodes() == null){
                            //如果该父节点的childrenTreeNode属性为空 new一个list 向该list中写入子节点
                            parent.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
                        }
                        parent.getChildrenTreeNodes().add((item));
                    }
                });
        return resultList;
    }
}
