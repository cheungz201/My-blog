package com.my.blog.website;

/**
 * @Program: my-blog
 * @Author: Zhang Zhe
 * @Create: 2021-08-30 17:27
 * @Version: 1.0.0
 * @Description:
 **/


public class Test {

    @org.junit.Test
    public void Test(){
        Thread thread = new Thread(() -> {
            System.out.println("线程一");
        });

        Thread thread1 = new Thread(() -> {
            System.out.println("线程二");
        });

        System.out.println("主线程");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        thread.start();
        thread1.start();
    }
}
