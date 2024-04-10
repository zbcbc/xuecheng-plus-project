package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.ucenter.mapper.XcMenuMapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcMenu;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: UserDetailsServiceImpl
 * Package: com.xuecheng.ucenter.service.impl
 * Description:
 *
 * @Author zbc
 * @Create 2024/4/8 15:50
 * @Version 1.0
 */
@Slf4j
@Component
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    XcUserMapper xcUserMapper;
    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    XcMenuMapper xcMenuMapper;

    /**
     * 传入的请求认证的参数就是AuthParamsDto
     * @param s
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        //将传入的json转成AuthParamsDto对象
        AuthParamsDto authParamsDto = null;
        try {
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        }catch (Exception e){
            throw new RuntimeException("请求认证参数不符合要求");
        }
        //认证类型，有password，wx...
        String authType = authParamsDto.getAuthType();
        //根据认证类型从spring中取出指定的bean，先注入spring容器ApplicationContext
        String beanName = authType + "_authservice";
        AuthService authService = applicationContext.getBean(beanName, AuthService.class);
        //调用认证方法
        XcUserExt xcUserExt = authService.execute(authParamsDto);

        UserDetails userDetails = getUserPrincipal(xcUserExt);
        return userDetails;
    }

    /**
     * 得到用户信息封装为UserDetails
     * @param xcUser
     * @return
     */
    public UserDetails getUserPrincipal(XcUserExt xcUser){
        //根据用户id查询用户的权限
        String[] authorities= {"test"};
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(xcUser.getId());
        if(xcMenus.size() > 0){
            List<String> permissions = new ArrayList<>();
            xcMenus.forEach(m -> {
                permissions.add(m.getCode());
            });
            authorities = permissions.toArray(new String[0]);
        }

        String password = xcUser.getPassword();
        //封装xcUserExt为userDetails
        //扩展令牌中的用户信息 可以将所有用户信息转为json 传入
        xcUser.setPassword(null);
        String userJson = JSON.toJSONString(xcUser);
        UserDetails userDetails = User.withUsername(userJson).password(password).authorities(authorities).build();

        return userDetails;
    }

}
