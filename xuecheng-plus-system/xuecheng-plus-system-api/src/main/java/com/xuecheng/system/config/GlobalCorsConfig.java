package com.xuecheng.system.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * @Create: 2024/2/11 - 15:13
 */
@Configuration
public class GlobalCorsConfig {
    @Bean
    public CorsFilter corsFilter(){
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*");//允许所有来源跨域访问
        corsConfiguration.setAllowCredentials(true);//允许跨域发送cookie
        corsConfiguration.addAllowedHeader("*");//放行全部原始头信息
        corsConfiguration.addAllowedMethod("*");//允许所有请求方法调用

        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration );
        CorsFilter corsFilter = new CorsFilter(urlBasedCorsConfigurationSource);

        return corsFilter;

    }
}
