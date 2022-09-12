package com.practice.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.practice.reggie.Handler.CustomException;
import com.practice.reggie.mapper.setMealMapper;
import com.practice.reggie.model.Setmeal;

import com.practice.reggie.model.SetmealDish;
import com.practice.reggie.model.dto.SetmealDto;
import com.practice.reggie.service.setMealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class setMealServiceImpl extends ServiceImpl<setMealMapper, Setmeal> implements setMealService{
    @Autowired
    private setMealDishServiceImpl setMealDishService;

    /**
     * 根据套餐id查询套餐及其菜品详情
     * @param id
     * @return
     */
    @Override
    @Transactional
    public SetmealDto getByIdWithDishDetails(Long id) {
        //1.创建返回对象
        SetmealDto setmealDto=new SetmealDto();
        //2.根据id查出setmeal
        Setmeal setmeal = super.getById(id);
        //拷贝
        BeanUtils.copyProperties(setmeal,setmealDto);
        //补充剩余的一个信息（categoryName不用补充，因为前端需要categoryId，跟list页面不同）
        LambdaQueryWrapper<SetmealDish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,id);
        queryWrapper.eq(SetmealDish::getIsDeleted,0);
        List<SetmealDish> list = setMealDishService.list(queryWrapper);
        setmealDto.setSetmealDishes(list);
        return setmealDto;
    }

    /**
     * 修改套餐及其菜品组成详细信息
     * @param setmealDto
     * @return
     */
    @Override
    @Transactional
    public boolean updateWithDishDetails(SetmealDto setmealDto) {
        //1.先修改setmeal
        boolean b = super.updateById(setmealDto);
        //2.先将原有相关套餐的内容先删除，再添加新的
        LambdaQueryWrapper<SetmealDish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealDto.getId());
        setMealDishService.remove(queryWrapper);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealDto.getId());
        }
        boolean b1 = setMealDishService.saveBatch(setmealDishes);
        if (b&&b1)
        {
            log.info("修改套餐信息已成功");
            return true;
        }
        return false;
    }

    /**
     * 添加套餐及其详细信息
     * @param setmealDto
     * @return
     */
    @Override
    @Transactional
    public boolean saveWithDishDetails(SetmealDto setmealDto) {
        //1.先添加套餐的基本信息
        boolean b = super.save(setmealDto);
        //2.再补充对应的mealDish信息
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            //给每一个mealDish数据添加上当前菜属于哪个套餐
            setmealDish.setSetmealId(setmealDto.getId());
        }
        boolean b1 = setMealDishService.saveBatch(setmealDishes);
        if (b&&b1)
        {
            log.info("添加套餐已成功");
            return true;
        }
        return false;
    }

    /**
     * 根据id删除套餐及其mealdish表
     * @param ids
     * @return
     */
    @Override
    @Transactional
    public boolean deleteWithDishDetails(ArrayList<Long> ids) {
        //也可以使用条件查询

//        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
//        queryWrapper.in(Setmeal::getId,ids); //在一个list中逐个遍历id对应查询
//        queryWrapper.eq(Setmeal::getStatus,1);
        //如果套餐正在售卖，不能删除，丢出异常
        for (Long id : ids) {
            Setmeal byId = super.getById(id);
            if (byId.getStatus()==1)
            {
                throw new CustomException("选中套餐正在售卖，不能删除");//抛出异常就直接跳过当前工作（方法）
            }
        }
        //不用管菜品和分类，可以直接删除
        for (Long id : ids) {
            //先"删除"setMeal
            Setmeal setmeal = new Setmeal();
            setmeal.setId(id);
            setmeal.setIsDeleted(1);
            boolean b = super.updateById(setmeal);

            //再"删除"setMealDish
            LambdaQueryWrapper<SetmealDish> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(SetmealDish::getSetmealId,id);
            SetmealDish setmealDish = new SetmealDish();
            setmealDish.setIsDeleted(1);
            boolean b1 = setMealDishService.update(setmealDish,queryWrapper);
            if (!b&&!b1)
                return false;
        }
        return true;
    }
}
