package com.my.blog.website.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Program: my-blog
 * @Author: Zhang Zhe
 * @Create: 2021-05-25 21:40
 * @Version: 1.0.0
 * @Description:
 **/

@RestController
public class Test {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @RequestMapping("/Hello")
    public String Hello() {
        Integer a = jdbcTemplate.queryForObject("select count(*) from t_metas", Integer.class);

        return a.toString();
    }
}
