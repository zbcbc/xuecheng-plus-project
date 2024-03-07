package com.xuecheng.base.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * @Create: 2024/2/6 - 16:08
 * 分页查询的分页参数
 */
@ToString
@Data
public class PageParams {
    /**
     * mybatis的参数接口类型是long 方便
     */
    @ApiModelProperty("页码")
    private Long pageNo = 1L;
    @ApiModelProperty("每页记录数")
    private Long pageSize = 10L;

    public PageParams() {
    }

    public PageParams(Long pageNo, Long pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

}
