package com.cosmo.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 设置图片访问路径
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    //TODO 本地  服务器
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/img/**").addResourceLocations("file:F:/img/cosmo/");
        registry.addResourceHandler("/img/**").addResourceLocations("file:/root/img/cosmo/");
    }
}
