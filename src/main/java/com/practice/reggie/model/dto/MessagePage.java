package com.practice.reggie.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
/*自定义分页返回对象，实际上可以使用系统自带Page对象*/
public class MessagePage<T> {
    private List<T> records;
    private long total;
}
