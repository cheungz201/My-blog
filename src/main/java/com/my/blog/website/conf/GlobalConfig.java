package com.my.blog.website.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.my.blog.website.filter.XssFilter;
import com.my.blog.website.utils.XssStringJsonSerializer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * @Program: my-blog
 * @Author: Zhang Zhe
 * @Create: 2022-03-10 11:12
 * @Version: 1.0.0
 * @Description: 一些全局bean
 **/


@Configuration
public class GlobalConfig {

    /**
     * 过滤json类型
     * @param builder
     * @return
     */
    @Bean
    @Primary
    public ObjectMapper xssObjectMapper(Jackson2ObjectMapperBuilder builder) {
        //解析器
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        //注册xss解析器
        SimpleModule xssModule = new SimpleModule("XssStringJsonSerializer");
        xssModule.addSerializer(new XssStringJsonSerializer());
        objectMapper.registerModule(xssModule);
        //返回
        return objectMapper;
    }


    /**
     * 注册filter
     * @return
     */
    @Bean
    public FilterRegistrationBean registerXssFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new XssFilter());
        registration.addUrlPatterns("/*");
        registration.setName(XssFilter.class.getSimpleName());
        registration.setOrder(1);
        return registration;
    }
}
