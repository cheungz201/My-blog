package com.my.blog.website.cache;

import java.util.concurrent.TimeUnit;

/**
 * 缓存的抽象接口
 *
 * @Program: my-blog
 * @Author: Zhang Zhe
 * @Create: 2022-04-28 20:40
 * @Version: 1.0.0
 **/
public interface StringCache {

    /**
     * 通过key获取在缓存中的指
     * @param key
     * @return 缓存中的值
     */
    public String getCache(String key);

    /**
     * 通过key删除在缓存中的值
     * @param key
     */
    public void deleteCache(String key);

    /**
     * 修改keu对应的value
     * @param key
     * @param value
     * @return 修改成功返回true，否则返回false
     */
    public boolean updateCache(String key, String value);

    /**
     * 增加缓存对
     * @param key
     * @param value
     */
    public void addCache(String key,String value);

    /**
     * 查看key是否有对应值
     * @param key
     * @return 存在返回true，否则返回false
     */
    public boolean exist(String key);

    /**
     * 增加缓存对并且指定过期时间
     * @param key
     * @param value
     * @param num
     * @param date
     */
    public void addCacheByTime(String key, String value, int num, TimeUnit date);
}
