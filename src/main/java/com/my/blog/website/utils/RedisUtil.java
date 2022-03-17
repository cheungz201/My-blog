package com.my.blog.website.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * detail
 *
 * @Program: my-blog
 * @Author: Zhang Zhe
 * @Create: 2022-03-17 12:26
 * @Version: 1.0.0
 **/

@Component
public class RedisUtil {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 缓存文章,默认缓存七天
     * @param key id
     * @param value contentVo
     */
    public void contentCache(String key, String value) {
        String cache;
        cache = redisTemplate.opsForValue().get(key);
        if (StringUtils.isBlank(cache)){
            synchronized ( this ){
                if (StringUtils.isBlank(redisTemplate.opsForValue().get(key))){
                    redisTemplate.opsForValue().set(key,value,7,TimeUnit.DAYS);
                }
            }
        }
    }

    /**
     * 判断是否key在redis中是否有缓存
     * @param key
     * @return
     */
    public boolean contentIsNull(String key){
        String oldContent = redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(oldContent)){
            return false;
        }
        return true;
    }

    /**
     * 返回缓存的值
     * @param key
     * @return
     */
    public String getCache(String key){
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 更新缓存文章
     * @param newKey
     * @param newValue
     */
    public void updateCache(String newKey,String newValue){
        if (redisTemplate.hasKey(newKey)) {
            redisTemplate.opsForValue().set(newKey,newValue,7,TimeUnit.DAYS);
        }
    }
}
