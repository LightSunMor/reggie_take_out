package com.practice.reggie.model.dto;

import com.practice.reggie.model.Dish;
import com.practice.reggie.model.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * 封装新实体类，用来接收前端新增菜品返回的 数据
 * dto:用于服务层和展示层之间的数据传输
 */
@Data
public class DishDto extends Dish {
    //DishDto是继承Dish的类，故除了以下三个属性外，原有的属性也都存在

    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName; //分类名,和分页列表前端页显示的参数prop一致，故设置来为了显示分页数据

    private Integer copies; //份数
}
