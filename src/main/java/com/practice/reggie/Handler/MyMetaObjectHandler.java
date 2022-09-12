package com.practice.reggie.Handler;

/*
* mybatis-plus的公共字段自动填充功能
* 1.在实体类的对应公共字段的属性上加注解，@TableField
* 2.实现一个组件，实现MetaObjectHandler，才能发挥自动填充的作用
* */

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.practice.reggie.util.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {
    //metaObject 可以自动把要提交修改或插入的数据封装进来
    // ThreadLocal 解决不能拿到动态的登录人id 的解决方法，利用线程单一的思想
    //使用封装的ThreadLocal工具类BaseContext来拿到id
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("INSERT"+metaObject.toString());
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime",LocalDateTime.now());
        metaObject.setValue("createUser", BaseContext.getCurrentId());
        metaObject.setValue("updateUser",BaseContext.getCurrentId());

    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("UPDATE"+metaObject.toString());
        metaObject.setValue("updateTime",LocalDateTime.now());
        metaObject.setValue("updateUser",BaseContext.getCurrentId());
    }
}
