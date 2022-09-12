package com.practice.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.practice.reggie.model.OrderDetail;
import com.practice.reggie.model.Orders;
import com.practice.reggie.model.R;
import com.practice.reggie.model.ShoppingCart;
import com.practice.reggie.model.dto.OrdersDto;
import com.practice.reggie.service.impl.shoppingCartServiceImpl;
import com.practice.reggie.util.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {
    @Resource
    private shoppingCartServiceImpl shoppingCartService;

    /**
     *获取购物车内商品的集合
     * @return
     */
    @GetMapping("/list")
    R<List<ShoppingCart>> selectCartList()
    {
        //设置搜索条件
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        queryWrapper.orderByDesc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        return R.success(list);
    }

    /**
     * 向购物车添加菜品,number默认为1
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    R<ShoppingCart> addDishOrSetMeal(@RequestBody ShoppingCart shoppingCart)
    {
        log.info("购物车添加商品，拿到数据：{}",shoppingCart.toString());
        shoppingCart.setUserId(BaseContext.getCurrentId());
        //判断该菜品是否在购物车中
        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper1=new LambdaQueryWrapper<>();
        if (dishId!=null)
        {
            //添加到购物车的是菜品
            queryWrapper1.eq(ShoppingCart::getDishId,dishId);
        }else {
            //添加到购物车的是套餐
            queryWrapper1.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        //绑定用户
        queryWrapper1.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        ShoppingCart one = shoppingCartService.getOne(queryWrapper1);
        if (one!=null)
        {
            one.setNumber(one.getNumber()+1);
            shoppingCartService.updateById(one);
        }
        else {
            shoppingCartService.save(shoppingCart);
            one=shoppingCart;
        }
        return R.success(one);
    }

    /**
     * 减少菜品或套餐数量
     * @param map
     * @return
     */
    @PostMapping("/sub")
    R<String> subDishOrMeal(@RequestBody Map map)
    {
        log.info("修改购物车，拿到的id：{}",map);
        Object dishId =  map.get("dishId"); //不能转化类型，会报空异常，就算不转换类型也不影响条件设立
        Object setmealId = map.get("setmealId");
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        if (dishId==null)
        {
            //修改的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,setmealId);
        }else {
            //修改的是菜品
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
        }
        ShoppingCart one = shoppingCartService.getOne(queryWrapper);
        if (one.getNumber()>1)
        {
            one.setNumber(one.getNumber()-1);
            shoppingCartService.updateById(one);
        }else {
            shoppingCartService.removeById(one);
        }
        return R.success("修改购物车数量成功");
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> cleanCart()
    {
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);
        return R.success("清空购物车成功");
    }

}
