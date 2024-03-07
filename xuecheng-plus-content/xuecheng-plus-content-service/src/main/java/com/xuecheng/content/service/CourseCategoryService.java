package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;

import java.util.List;

/**
 * @Create: 2024/2/17 - 17:22
 */

public interface CourseCategoryService {
    /**\
     * 课程分类树形结构
     * @param id
     * @return
     */
    public List<CourseCategoryTreeDto> queryTreeNodes(String id );
}
