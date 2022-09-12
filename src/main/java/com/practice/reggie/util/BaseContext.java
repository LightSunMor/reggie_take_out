package com.practice.reggie.util;
/*
* 基于ThreadLocal封装工具类
* */
public class BaseContext {
    private static ThreadLocal<Long> threadLocal=new ThreadLocal<>();
    public static void setCurrentId(Long id)
    {
        threadLocal.set(id);
    }
    public static Long getCurrentId()
    {
        return threadLocal.get();
    }
}
