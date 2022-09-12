package com.practice.reggie.controller;

import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.practice.reggie.model.Category;
import com.practice.reggie.model.Dish;
import com.practice.reggie.model.DishFlavor;
import com.practice.reggie.model.dto.DishDto;
import com.practice.reggie.model.R;
import com.practice.reggie.service.impl.categoryServiceImpl;
import com.practice.reggie.service.impl.dishFlavorServiceImpl;
import com.practice.reggie.service.impl.dishServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/dish")
/**
 * 菜品添加，不仅要改变dish表还要改变dish_flavor表
 */
public class DishController {
    @Autowired
    private dishServiceImpl dishService;
    @Autowired
    private dishFlavorServiceImpl dishFlavorService;
    @Resource
    private categoryServiceImpl categoryService;


    /**
     * 菜品页面分页请求接口
     * @return
     */
    @GetMapping("/page")
    R<Page> findByPage_dish(Long page,Long pageSize,String name)
    {
        //创建两个Page对象
        Page<Dish> page_obj=new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage=new Page<>();

        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.like(!StringUtils.isEmpty(name),Dish::getName,name);
        //1.是否被删除
        queryWrapper.eq(Dish::getIsDeleted,0);
        //2.排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        //3.查询
        dishService.page(page_obj, queryWrapper);
        //4.封装新page赋值
        //对象拷贝,不拷贝(忽略)records
        BeanUtils.copyProperties(page_obj,dishDtoPage,"records");
        //5.自己处理records !!!
        List<Dish> records1 = page_obj.getRecords();
        //使用records1的stream流对其中的数据一一处理(遍历)，返回一个全新的list(可以和原来不同类型的list)
        List<DishDto> records2=records1.stream().map((item)->{
            //创建dishDto对象，且拷贝原dish的数据
            DishDto dishDto=new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            //拿到分类名
            Category byId = categoryService.getById(item.getCategoryId());
            if (byId!=null)
            {
                String byIdName = byId.getName();
                //设置分类名
                dishDto.setCategoryName(byIdName);
            }
            return dishDto;
        }).collect(Collectors.toList());

        //将records2赋给dishDto的page对象
        dishDtoPage.setRecords(records2);

        //3.返回结果
        return R.success(dishDtoPage);

    }

    /**
     * 新增菜品接口
     * @param dishDto
     * @return
     */
    @PostMapping
    R<String> insert_dish(@RequestBody DishDto dishDto)
    {
        //需要分出不同的信息存储在两张表中,方法可以直接在controller分别封装存储/也可以在dishService中自定义新方法进行存储
        log.info("拿到dishDto：{}",dishDto);
        boolean b = dishService.saveWithInFlavor(dishDto);
        if (b)
        {
            log.info("新增菜品已成功");
            return R.success("新增菜品成功");
        }
        return R.error("新增菜品失败");
    }

    /**
     * 通过id查询菜品及其口味
     * @return
     */
    @GetMapping("/{id}")
    R<DishDto> selectOneById_dish(@PathVariable("id")Long id)
    {
        //使用封装的新service方法
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        log.info("回查菜品和口味详细信息已完成");
        return R.success(dishDto);
    }

    /**
     * 修改菜品信息
     * @return
     */
    @PutMapping
    R<String> update_dish(@RequestBody DishDto dishDto)
    {
        boolean b = dishService.updateWithFlavor(dishDto);
        if (b)
        {
            log.info("修改菜品已成功");
            return R.success("修改菜品成功");
        }
        return R.error("修改菜品失败");
    }

    /**
     * （批量）修改菜品的售卖状态的接口
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{sta}")
    R<String> dishStatusByStatus(@PathVariable("sta") Integer status,@RequestParam ArrayList<Long> ids)
    {
        System.out.println("拿到的要改变为的状态值"+status);
        System.out.println("修改菜品状态拿到的id："+ids);
        //封装更新数据
        ArrayList<Dish> dishList=new ArrayList<>();
        for (Long id : ids) {
            Dish dish = new Dish();
            dish.setId(id);
            dish.setStatus(status);
            dishList.add(dish);
        }

        boolean b = dishService.updateBatchById(dishList);
        if (b)
        {
            log.info("修改菜品状态成功");
            return R.success("修改状态成功");
        }
        return R.error("修改状态失败");
    }

    /**
     * （批量）删除dish及其详细信息的接口
     * @param ids
     * @return
     */
    @DeleteMapping()
    R<String> delete_dish(@RequestParam ArrayList<Long> ids)
    {
        log.info("删除菜品，拿到id：{}",ids);
        //自定义删除方法
        boolean b = dishService.deleteWithFlavorAndSetmeal(ids);
        if (b)
            return R.success("删除菜品成功");
        return R.error("删除菜品失败");
    }

    /**
     * 根据菜品分类id拿到对应的菜品，返回dishDto(包含口味，在user的index必要,如果不包含口味，在前端就显示不了口味选择)
     * @param categoryId,name
     * @return
     */
    @GetMapping("/list")
    R<List<DishDto>> queryDishList(Long categoryId,String name)
    {
        log.info("套餐页面查菜品列表的接口，拿到参数分类id:{}",categoryId);
        log.info("套餐页面查菜品列表的接口，拿到参数dishName:{}",name);
        List<Dish> list =null;
        List<DishDto> dishDtoList=null;
        if (categoryId==null)
        {
            //1.仅根据搜索框中的name搜索
            LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.like(Dish::getName,name);
            queryWrapper.orderByAsc(Dish::getSort);
            queryWrapper.orderByDesc(Dish::getUpdateTime);
            //且菜品要处于启售状态
            queryWrapper.eq(Dish::getStatus,1);
            list = dishService.list(queryWrapper);

            dishDtoList = list.stream().map(item -> {
                DishDto dishDto = new DishDto();
                BeanUtils.copyProperties(item, dishDto);
                //封装口味，dish的id
                LambdaQueryWrapper<DishFlavor> queryWrapper1 = new LambdaQueryWrapper<>();
                queryWrapper1.eq(DishFlavor::getDishId, item.getId());
                queryWrapper1.eq(DishFlavor::getIsDeleted, 0);
                queryWrapper1.orderByAsc(DishFlavor::getUpdateTime);
                List<DishFlavor> dishFlavors = dishFlavorService.list(queryWrapper1);
                dishDto.setFlavors(dishFlavors);
                return dishDto;
            }).collect(Collectors.toList());
        }else {
            //2.仅根据分类id来搜索
            LambdaQueryWrapper<Dish> queryWrapper1=new LambdaQueryWrapper<>();
            queryWrapper1.eq(Dish::getCategoryId,categoryId);
            queryWrapper1.orderByAsc(Dish::getSort);
            queryWrapper1.orderByDesc(Dish::getUpdateTime);
            queryWrapper1.eq(Dish::getStatus,1);
            list = dishService.list(queryWrapper1);

            dishDtoList = list.stream().map(item -> {
                DishDto dishDto = new DishDto();
                BeanUtils.copyProperties(item, dishDto);
                //封装口味，dish的id
                LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(DishFlavor::getDishId, item.getId());
                queryWrapper.eq(DishFlavor::getIsDeleted, 0);
                queryWrapper.orderByAsc(DishFlavor::getUpdateTime);
                List<DishFlavor> dishFlavors = dishFlavorService.list(queryWrapper);
                dishDto.setFlavors(dishFlavors);
                return dishDto;
            }).collect(Collectors.toList());
        }
        log.info("查询出菜品为：{}",dishDtoList);
        return R.success(dishDtoList);
    }
}
