package com.practice.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.practice.reggie.mapper.setMealDishMapper;
import com.practice.reggie.model.SetmealDish;
import com.practice.reggie.service.setMealDishService;
import com.practice.reggie.service.setMealService;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Service;

@Service
public class setMealDishServiceImpl extends ServiceImpl<setMealDishMapper, SetmealDish> implements setMealDishService {
}
