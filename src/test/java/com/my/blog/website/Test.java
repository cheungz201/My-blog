package com.my.blog.website;

import com.my.blog.website.utils.TaleUtils;

import java.io.FileNotFoundException;

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


    @org.junit.Test
    public void testFun(){
        System.out.println(TaleUtils.getFileKey("xxx.txt"));
    }

}
