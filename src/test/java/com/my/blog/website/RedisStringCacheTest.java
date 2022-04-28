package com.my.blog.website;

import com.my.blog.website.cache.RedisStringCache;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * detail
 *
 * @Program: my-blog
 * @Author: Zhang Zhe
 * @Create: 2022-04-28 21:24
 * @Version: 1.0.0
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisStringCacheTest {

    @Resource
    private RedisStringCache cache;

    @Test
    public void cacheTest(){
        cache.addCache("test","test");
    }

    @Test
    public void cacheTimeTest(){
        cache.addCacheByTime("test","test1",1, TimeUnit.DAYS);
    }
}
