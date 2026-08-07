package com.sakana.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class CorsConfig implements WebMvcConfigurer {  // WebMvcConfigurer是spring mvc的配置接口。

    //配置跨域请求
    /**
     * 配置跨域请求
     * @param registry 跨域注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry.addMapping("/**")   //代表所有请求路径
                .allowedOrigins("http://localhost:5173")   //允许从这个来源的请求访问
                .allowedMethods("*")                   // 允许所有请求方法( get, post, delete,put,...）
                .allowedHeaders("*")                   // 允许所有请求头
                .allowCredentials(true)                // 允许携带cookie
                .maxAge(3600);                        // 最大缓存时间，单位秒
    }
}
