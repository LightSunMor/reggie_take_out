package com.practice.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.practice.reggie.model.Dish;
import com.practice.reggie.model.dto.DishDto;

import java.util.ArrayList;

public interface dishService extends IService<Dish> {

    public boolean saveWithInFlavor(DishDto dishDto);

    public DishDto getByIdWithFlavor(Long id);
    public boolean updateWithFlavor(DishDto dishDto);

    boolean deleteWithFlavorAndSetmeal(ArrayList<Long> ids);
}
