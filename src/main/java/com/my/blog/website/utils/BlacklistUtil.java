package com.my.blog.website.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 黑名单工具类,一秒钟的访问次数超过阈值则视为不正常
 *
 *
 * @Author: Zhang Zhe
 * @CreateTime: 2022-06-17 11:22
 */
public class BlacklistUtil {

    private static final Logger log = LoggerFactory.getLogger(BlacklistUtil.class);

    /**
     * 黑名单文件路径
     */
    private static final String blackPath = "/usr/local/blog-website/other/black.txt";

    /**
     * ip地址和访问次数的映射
     */
    private static ConcurrentHashMap<String,BlackBean> map = new ConcurrentHashMap<String,BlackBean>();

    /**
     * 延时ip列表
     */
    private static DelayList delayList = new DelayList();

    /**
     * 封禁ip
     */
    private static CopyOnWriteArrayList<String> blackList = new CopyOnWriteArrayList<String>();

    /**
     * 初始化黑名单
     */
    static{
        try {
            FileReader fileReader = new FileReader(blackPath);
            BufferedReader bufferedReader =new BufferedReader(fileReader);
            String str;
            while( (str = bufferedReader.readLine()) != null){
                if (str.trim().length() >= 2){
                    blackList.add(str);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断ip是否为黑名单
     * @param ip
     * @return
     */
    public static boolean isBlack(String ip){
        return blackList.contains(ip);
    }

    /**
     * 超过阈值将拉入黑名单,此处的频率是每分钟的访问次数不能超过50次
     * @param ip
     * @return 封禁返回true,否则返回false
     */
    public static boolean exceedThreshold(String ip){
        BlackBean old = map.get(ip);
        countAdd(ip);
        BlackBean bean = null;
        // 如果bean不为空且黑名单中没有这个ip则删除掉这个ip，重新计数
        if ((bean = delayList.get(old)) != null && (!blackList.contains(bean))){
            map.remove(ip);
        }
        // 频率不能大于一秒钟50次,否则拉黑
        if(bean != null && bean.getCount() >= 50){
            addBlackList(ip);
            return true;
        }
        return false;
    }

    /**
     * 添加一个ip地址添加到黑名单中，首先将ip添加进内存中，然后把黑名单写进文件中，最后删除掉map中的ip。
     * 需要注意的是这里是同步的,将ip写进文件时应该不允许任何线程操作blackList，否则会出现内存值与文件值不同的情况
     * @param ip
     */
    public synchronized static void addBlackList(String ip){
        if (blackList.contains(ip)){
            return;
        }
        log.info("拉黑ip = {}",ip);
        blackList.add(ip);
        flushBlack2File(ip);
        map.remove(ip);
    }

    /**
     * 计数加1，如果当前ip是第一次访问则放入一个新的DelayBean计数，计数时间为1s
     * @param ip
     */
    public synchronized static void countAdd(String ip){
        BlackBean blackBean = map.get(ip);
        if (blackBean != null){
            blackBean.setCount(blackBean.getCount()+1);
        }else {
            BlackBean delay = new BlackBean(ip,1,1000L);
            map.put(ip,delay);
            delayList.add(delay);
        }
    }

    /**
     * 将指定ip添加进文件中
     * @param ip
     */
    private static void flushBlack2File(String ip){
        try {
            File file = new File(blackPath);
            RandomAccessFile randomAccessFile=new RandomAccessFile(file,"rw");
            randomAccessFile.seek(file.length());
            randomAccessFile.write((ip + "\n").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class BlackBean {
        private String ip;
        private Integer count;
        private Long expired;

        public BlackBean(String ip, Integer count, Long time) {
            this.ip = ip;
            this.count = count;
            this.expired = System.currentTimeMillis() + time;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public Long getExpired() {
            return expired;
        }
    }

    static class DelayList extends CopyOnWriteArrayList<BlackBean>{

        /**
         * 必须超过指定时间才能取出数据
         * @param blackBean
         * @return
         */
        public BlackBean get(BlackBean blackBean) {
            BlackBean bean = null;
            int i = super.indexOf(blackBean);
            if (i >= 0){
                bean = super.get(i);
            }
            if (bean == null || bean.expired >= System.currentTimeMillis()){
                return null;
            }
            return bean;
        }

    }
}
