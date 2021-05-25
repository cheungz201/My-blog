package com.my.blog.website.conf;

import com.alibaba.druid.support.http.StatViewServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Program: my-blog
 * @Author: Zhang Zhe
 * @Create: 2021-05-25 21:21
 * @Version: 1.0.0
 * @Description:
 **/

@Configuration
public class DruidConfig {

    @Bean
    public ServletRegistrationBean servletRegistrationBean(){
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(new StatViewServlet(),"/druid/*");
        return servletRegistrationBean;
    }

}
