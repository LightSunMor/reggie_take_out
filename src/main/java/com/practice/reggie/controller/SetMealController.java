package com.practice.reggie.controller;

import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.practice.reggie.model.*;
import com.practice.reggie.model.dto.DishDto;
import com.practice.reggie.model.dto.SetmealDto;
import com.practice.reggie.service.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/setmeal")
public class SetMealController {
    @Autowired
    private setMealServiceImpl setMealService;
    @Autowired
    private categoryServiceImpl categoryService;
    @Autowired
    private setMealDishServiceImpl setMealDishService;
    @Autowired
    private dishServiceImpl dishService;
    @Autowired
    private dishFlavorServiceImpl dishFlavorService;

    /**
     * 套餐页面分页展示
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    R<Page> selectByPage(Long page,Long pageSize,String name)
    {
        Page<Setmeal> setmealPage=new Page<>(page,pageSize);
        Page<SetmealDto> setmealDtoPage=new Page<>();
        //搜索条件
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        //是否被删除
        queryWrapper.eq(Setmeal::getIsDeleted,0);
        //排序
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        queryWrapper.like(!StringUtils.isEmpty(name),Setmeal::getName,name);
        //查询结果
        setMealService.page(setmealPage,queryWrapper);
        //先将出records以外的内容赋给DtoPage
        BeanUtils.copyProperties(setmealPage,setmealDtoPage,"records");
        //再将dtopage中的record进行处理
        List<Setmeal> records1 = setmealPage.getRecords();

        List<SetmealDto> records2=records1.stream().map((item)->{
            //创建新SetmealDto对象
            SetmealDto setmealDto=new SetmealDto();
            //将原setMeal对象的基本数据传给他
            BeanUtils.copyProperties(item,setmealDto);
            //拿到分类id对应的对象
            Category byId = categoryService.getById(item.getCategoryId());
            if (byId!=null)
            {
                //设置分类名到dto对象
                setmealDto.setCategoryName(byId.getName());
            }
            return setmealDto;
        }).collect(Collectors.toList());
        //再把records2赋给DtoPage
        setmealDtoPage.setRecords(records2);

        return R.success(setmealDtoPage);
    }

    /**
     * 根据套餐id查询套餐的详细信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    R<SetmealDto> selectOneById(@PathVariable("id") Long id)
    {
        SetmealDto byIdWithDishDetails = setMealService.getByIdWithDishDetails(id);
        log.info("回查套餐及其菜品组成信息已成功");
        return R.success(byIdWithDishDetails);
    }

    /**
     * 修改套餐及其菜品组成详细信息
     * @param setmealDto
     * @return
     */
    @PutMapping()
    R<String> update_setMeal(@RequestBody SetmealDto setmealDto)
    {
        log.info("修改套餐的内容为：{}",setmealDto);
        boolean b = setMealService.updateWithDishDetails(setmealDto);
        if (b)
            return R.success("修改套餐成功");
        return R.error("修改套餐失败");
    }

    /**
     * 新增套餐及其菜品组成
     * @param setmealDto
     * @return
     */
    @PostMapping()
    R<String> insert_setMeal(@RequestBody SetmealDto setmealDto)
    {
        log.info("要添加的套餐内容是：{}",setmealDto);
        boolean b = setMealService.saveWithDishDetails(setmealDto);
        if (b)
        {
            return R.success("添加套餐成功");
        }
        return R.error("添加套餐失败");
    }

    /**
     * （批量）根据id删除套餐及其mealdish表
     * @param ids
     * @return
     */
    @DeleteMapping()
    R<String> delete_setMeal(@RequestParam ArrayList<Long> ids)
    {
        log.info("删除套餐，拿到id：{}",ids);
        boolean b = setMealService.deleteWithDishDetails(ids);
        if (b)
            return R.success("删除套餐成功");
        return R.error("删除套餐失败");
    }

    /**
     * （批量）修改套餐状态栏
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{sta}")
    R<String> setmealStatusByStatus(@PathVariable("sta") Integer status,@RequestParam ArrayList<Long> ids)
    {
        log.info("拿到请求改为的状态是:{}",status);
        log.info("修改状态的套餐的id:{}",ids);
        ArrayList<Setmeal> setmealList=new ArrayList<>();
        for (Long id : ids) {
            Setmeal setmeal = new Setmeal();
            setmeal.setId(id);
            setmeal.setStatus(status);
            setmealList.add(setmeal);
        }
        //整个list修改状态
        boolean b = setMealService.updateBatchById(setmealList);
        if (b)
            return R.success("修改状态成功");
        return R.error("修改状态失败");
    }

    /**
     * 根据分类id和状态拿到套餐list
     * @param categoryId
     * @param status
     * @return
     */
    @GetMapping("/list")
    R<List<Setmeal>> SelectSetMealList(Long categoryId,Integer status)
    {
        log.info("移动端查询套餐list，拿到categoryId和status为{}，{}",categoryId,status);
        //1.根据id和status查询满足条件的套餐
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Setmeal::getStatus,status);
        queryWrapper.eq(Setmeal::getIsDeleted,0);
        queryWrapper.eq(Setmeal::getCategoryId,categoryId);
        List<Setmeal> list = setMealService.list(queryWrapper);
        log.info("查出的套餐list是{}",list);
        if (list == null) {
            return R.error("没有该分类的套餐啦");
        }
        return R.success(list);
    }

    /**
     * 购物车套餐详细菜品
     * @param id
     * @return
     */
    @GetMapping("/dish/{id}")
    R<List<DishDto>> setMealDishDetails(@PathVariable("id") Long id)
    {
        log.info("购物车套餐详细，套餐的id：{}",id);
        LambdaQueryWrapper<SetmealDish> queryWrapper=new LambdaQueryWrapper<>();
        //查询条件,查出套餐详细情况
        queryWrapper.eq(SetmealDish::getSetmealId,id);
        queryWrapper.eq(SetmealDish::getIsDeleted,0);
        queryWrapper.orderByAsc(SetmealDish::getSort);
        queryWrapper.orderByDesc(SetmealDish::getUpdateTime);
        List<SetmealDish> list = setMealDishService.list(queryWrapper);
        // 使用stream流封装dishDto
        List<DishDto> collect_Dto = list.stream().map(item -> {
            Dish dish = dishService.getById(item.getDishId());
            DishDto dishDto = new DishDto();
            //拷贝dish
            BeanUtils.copyProperties(dish, dishDto);
            LambdaQueryWrapper<DishFlavor> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(DishFlavor::getDishId, dish.getId());
            queryWrapper1.eq(DishFlavor::getIsDeleted, 0);
            //装配菜品口味
            List<DishFlavor> list1 = dishFlavorService.list(queryWrapper1);
            dishDto.setFlavors(list1);
            dishDto.setCopies(item.getCopies());
            return dishDto;
        }).collect(Collectors.toList());

        return R.success(collect_Dto);
    }
}
