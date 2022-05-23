package com.my.blog.website.conf;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @Program: my-blog
 * @Author: Zhang Zhe
 * @Create: 2021-05-25 21:21
 * @Version: 1.0.0
 * @Description: druid的配置类
 **/

@Configuration
public class DruidConfig {

    @Value("${druid.username}")
    private String username;

    @Value("${druid.password}")
    private String password;

    /**
     * 过滤器配置
     * @return
     * @throws SQLException
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() throws SQLException {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setFilters("stat");
        druidDataSource.setFilters("wall");
        return druidDataSource;
    }

    /**
     * 监控页配置
     * @return
     */
    @Bean
    public ServletRegistrationBean servletRegistrationBean(){
        ServletRegistrationBean servletRegistrationBean = new
                ServletRegistrationBean(new StatViewServlet(),"/druid/*");
        Map<String, String> initParameters = new HashMap<>();
        // 禁用HTML页面上的“Rest All”功能
        initParameters.put("resetEnable", "true");
        // 监控页面登录用户名
        initParameters.put("loginUsername", username);
        // 监控页面登录用户密码
        initParameters.put("loginPassword", password);
        // ip黑名单,如果某个ip同时存在，deny优先于allow
        initParameters.put("deny", "");
        // ip白名单（没有配置或者为空，则允许所有访问）
        initParameters.put("allow", "");
        // 不统计的文件和路径
        initParameters.put("exclusions","/static/*,*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
        servletRegistrationBean.setInitParameters(initParameters);
        return servletRegistrationBean;

    }

    /**
     * 配置web监控的filter
     * @return
     */
    @Bean
    public FilterRegistrationBean webStatFilter(){
        FilterRegistrationBean bean = new FilterRegistrationBean();
        bean.setFilter(new WebStatFilter());
        Map<String,String> initParams = new HashMap<>();
        initParams.put("exclusions","/static/*,*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
        bean.setInitParameters(initParams);
        bean.setUrlPatterns(Arrays.asList("/*"));
        return bean;
    }



}
