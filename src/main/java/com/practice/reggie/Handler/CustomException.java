package com.practice.reggie.Handler;
/*
* 自定义  业务异常类
* 运行时异常RuntimeException
* */
public class CustomException extends RuntimeException{
    public CustomException(String msg)
    {
        super(msg);
    }
}
