package com.my.blog.website;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.my.blog.website.utils.TaleUtils;
import xyz.cheungz.httphelper.constant.HttpConstant;
import xyz.cheungz.httphelper.core.ResolveDataHttpClient;
import xyz.cheungz.httphelper.core.multithreading.MultiHttpClient;

import java.io.*;
import java.util.*;

/**
 * @Program: my-blog
 * @Author: Zhang Zhe
 * @Create: 2021-08-30 17:27
 * @Version: 1.0.0
 * @Description:
 **/


public class Test {

    //@org.junit.Test
    public void Test(){
    }


    //@org.junit.Test
    public void taleTest() throws FileNotFoundException {
        System.out.println(TaleUtils.getUploadFilePath());
    }


    //@org.junit.Test
    public void testFun(){
        System.out.println(TaleUtils.getFileKey("xxx.txt"));
    }

    //@org.junit.Test
    public void fun(){
        String path = TaleUtils.class.getResource("/").getPath()+"application-jdbc.properties";
        System.out.println(path);
        File file = new File(path);
        try (InputStream inputStream = new FileInputStream(file)) {
            Properties properties = new Properties();
            properties.load(inputStream);
            Set<Object> objects = properties.keySet();
            Iterator<Object> iterator = objects.iterator();
            while (iterator.hasNext()){
                System.out.println(iterator.next());
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @org.junit.Test
    public void multiSenTest(){
        ResolveDataHttpClient client = new ResolveDataHttpClient(new MultiHttpClient());
        /*Map map = new HashMap();
        map.put("username","Zhang San");
        try {
            System.out.println(client.sendPost("http://localhost:8080/json", HttpConstant.BODY, map));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }*/

        System.out.println(client.sendGet("http://www.cheungz.xyz/article/26"));
    }

}
