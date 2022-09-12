package com.practice.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.practice.reggie.Handler.CustomException;
import com.practice.reggie.mapper.categoryMapper;
import com.practice.reggie.model.Category;
import com.practice.reggie.model.Dish;
import com.practice.reggie.model.Setmeal;
import com.practice.reggie.service.categoryService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class categoryServiceImpl extends ServiceImpl<categoryMapper, Category> implements categoryService {
    @Autowired
    private dishServiceImpl dishService;
    @Autowired
    private setMealServiceImpl setMealService;

    /**
     * 根据id删除相关分类，删除之前需要进行判断，
     * 删除的分类是否关联了相应的菜品或者套餐
     * @param id
     */
    @Override
    @Transactional
    public boolean removeById(Long id) {
        //当前分类是否关联了菜品，如果已经关联，抛出一个业务异常
        LambdaQueryWrapper<Dish> queryWrapper_dish=new LambdaQueryWrapper<>();
        queryWrapper_dish.eq(Dish::getCategoryId,id);
        int count = dishService.count(queryWrapper_dish);
        if (count>0)
        {
            //确认关联，需要抛出一个异常
            throw new CustomException("当前分类下关联了菜品，不能删除"); //为了能在前端显示出来，用全局异常捕获器GlobalExceptionHandler来捕获这个异常
        }

        //查询当前分类是否关联了套餐，如果已经关联，抛出一个业务异常
        LambdaQueryWrapper<Setmeal> queryWrapper_meal=new LambdaQueryWrapper<>();
        queryWrapper_meal.eq(Setmeal::getCategoryId,id);
        int count1 = setMealService.count(queryWrapper_meal);
        if (count1>0)
        {
            //确认关联，需要抛出异常
            throw new CustomException("当前分类下关联了套餐，不能删除");
        }

        //否则正常删除
        Category category=new Category();
        category.setId(id);
        category.setIsDeleted(1);
        return super.updateById(category);
    }
}
