package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;

/**
 * ClassName: AuthService
 * Package: com.xuecheng.ucenter.service
 * Description: 统一认证接口
 *
 * @Author zbc
 * @Create 2024/4/8 17:01
 * @Version 1.0
 */
public interface AuthService {
    /**
     * 认证方法
     * @param authParamsDto
     * @return
     */
    XcUserExt execute(AuthParamsDto authParamsDto);
}
