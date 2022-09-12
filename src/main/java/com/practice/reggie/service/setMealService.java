package com.practice.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.practice.reggie.model.Setmeal;
import com.practice.reggie.model.dto.DishDto;
import com.practice.reggie.model.dto.SetmealDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public interface setMealService extends IService<Setmeal> {
    SetmealDto getByIdWithDishDetails(Long id);

    boolean updateWithDishDetails(SetmealDto setmealDto);

    boolean saveWithDishDetails(SetmealDto setmealDto);

    boolean deleteWithDishDetails(ArrayList<Long> ids);
}
