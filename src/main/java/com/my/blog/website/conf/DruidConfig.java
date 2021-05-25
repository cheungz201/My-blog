package com.my.blog.website.conf;

import com.alibaba.druid.support.http.StatViewServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

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

        Map<String, String> initParameters = new HashMap<>();

        initParameters.put("resetEnable", "false"); //禁用HTML页面上的“Rest All”功能

        initParameters.put("allow", ""); //ip白名单（没有配置或者为空，则允许所有访问）

        initParameters.put("loginUsername", "admin"); //++监控页面登录用户名

        initParameters.put("loginPassword", "xxx"); //++监控页面登录用户密码

        initParameters.put("deny", ""); //ip黑名单

        //如果某个ip同时存在，deny优先于allow

        servletRegistrationBean.setInitParameters(initParameters);

        return servletRegistrationBean;

    }



}
