package com.my.blog.website.cache;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * redis缓存实现
 *
 * @Program: my-blog
 * @Author: Zhang Zhe
 * @Create: 2022-04-28 20:50
 * @Version: 1.0.0
 **/
@Component
public class RedisStringCache implements StringCache {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public String getCache(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    @Override
    public void deleteCache(String key) {
        stringRedisTemplate.delete(key);
    }

    @Override
    public boolean updateCache(String key, String value) {
        stringRedisTemplate.opsForValue().set(key,value);
        return false;
    }

    @Override
    public void addCache(String key, String value) {
        stringRedisTemplate.opsForValue().set(key,value);
    }

    @Override
    public boolean exist(String key) {
        if (StringUtils.isNotBlank(stringRedisTemplate.opsForValue().get(key))) {
            return true;
        }
        return false;
    }

    @Override
    public void addCacheByTime(String key, String value, int num, TimeUnit date) {
        stringRedisTemplate.opsForValue().set(key,value,num,date);
    }

}
