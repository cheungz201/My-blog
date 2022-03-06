package com.my.blog.website.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * @Program: druid-test
 * @Author: Zhang Zhe
 * @Create: 2021-09-19 15:16
 * @Version: 1.0.0
 * @Description:
 **/

public class JsonUtil {

    private static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 序列化,将obj转成符合json格式的string
     * @param t：需要序列化的对象
     * @return 返回一个符合json格式的string
     * @throws JsonProcessingException
     */
    public static <T> String obj2String(T t) throws JsonProcessingException {
        return t instanceof String ? (String) t:objectMapper.writeValueAsString(t);
    }

    /**
     * 反序列化,将符合json格式的string转成obj
     * @param str：需要反序列化的字符串
     * @param tClass:指定返回对象类型
     * @return 返回越一个对象
     * @throws JsonProcessingException
     */
    public static <T> T string2Obj(String str,Class<T> tClass) throws IOException {
        return objectMapper.readValue(str,tClass);
    }
}