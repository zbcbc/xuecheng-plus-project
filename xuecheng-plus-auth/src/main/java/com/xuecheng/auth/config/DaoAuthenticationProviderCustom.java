package com.xuecheng.auth.config;

import com.xuecheng.ucenter.service.impl.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * ClassName: DaoAuthenticationProviderCustom
 * Package: com.xuecheng.auth.config
 * Description: 重写了DaoAuthenticationProvider的校验密码方法，因为我们统一认证入口，有些认证不需要校验密码
 *
 * @Author zbc
 * @Create 2024/4/8 16:46
 * @Version 1.0
 */
@Component
public class DaoAuthenticationProviderCustom extends DaoAuthenticationProvider {
    //要调用UserDetailsService 且UserDetailsService被我们自己重写了 所以采用注入的方法注入进来
    @Autowired
    public void setUserDetailsService(UserDetailsServiceImpl userDetailsServiceImpl) {
        super.setUserDetailsService(userDetailsServiceImpl);
    }

    //重写为空 不用校验密码，需要校验密码的时候再用我们自己写的
    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        //super.additionalAuthenticationChecks(userDetails, authentication);
    }
}
