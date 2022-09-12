package com.practice.reggie.model;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;

@Data
public class R<T> {  /* 通用结果类*/

    private Integer code; //编码：1成功，0和其它数字为失败

    private String msg; //错误信息

    private T data; //数据

    private Map map = new HashMap(); //动态数据

    public static <T> R<T> success(T object) {  //为什么要加上两个泛型限制，因为限制为静态方法，方便R类直接调用
        R<T> r = new R<T>();
        r.data = object;
        r.code = 1;
        return r;
    }

    public static <T> R<T> error(String msg) {
        R r = new R();
        r.msg = msg;
        r.code = 0;
        return r;
    }

    public R<T> add(String key, Object value) {
        this.map.put(key, value);
        return this;
    }

}
