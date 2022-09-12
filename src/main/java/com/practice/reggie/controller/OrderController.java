package com.practice.reggie.controller;

import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.practice.reggie.model.OrderDetail;
import com.practice.reggie.model.Orders;
import com.practice.reggie.model.R;
import com.practice.reggie.model.dto.OrdersDto;
import com.practice.reggie.service.impl.orderDetailServiceImpl;
import com.practice.reggie.service.impl.ordersServiceImpl;
import com.practice.reggie.util.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private ordersServiceImpl ordersService;
    @Autowired
    private orderDetailServiceImpl orderDetailService;
    /**
     * 管理端订单分页查询,如果这里Page对象使用OrderDto，那么editOrderStatus这个接口就可省略
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("/page")
    R<Page> selectByPage_Employee(Long page,Long pageSize,@RequestParam(required = false) String number,@RequestParam(required = false)String beginTime,@RequestParam(required = false)String endTime)
    {
        log.info("订单分页页面拿到的参数：page-{}，pageSize-{},number-{},beginTime-{},endTime-{}",page,pageSize,number,beginTime,endTime);
        //1.创建Page对象
        Page<Orders> ordersPage=new Page<>(page,pageSize);
        //2.创建条件过滤器
        LambdaQueryWrapper<Orders> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.like(org.apache.commons.lang.StringUtils.isNotEmpty(number),Orders::getNumber,number);
        queryWrapper.orderByDesc(Orders::getOrderTime);
        //3.如果日期存在，设置日期转换器，设置格式，否则不操作，会报错（拿不会）
        if (beginTime!=null&&endTime!=null)
        {
            DateTimeFormatter dateTimeFormatter=DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime beginTimeNew = LocalDateTime.parse(beginTime, dateTimeFormatter);
            LocalDateTime endTimeNew = LocalDateTime.parse(endTime, dateTimeFormatter);
            queryWrapper.between(Orders::getOrderTime,beginTimeNew,endTimeNew);
        }
        //4.查询条件设置好，开始查询
        ordersService.page(ordersPage,queryWrapper);
        //5.查询结束，输出和返回结果
        log.info("订单页面查询信息：{}",ordersPage.getRecords());
        return R.success(ordersPage);
    }

    /**
     * 管理端，查看订单详细信息接口
     * @param id
     * @return
     */
    @GetMapping("/Detail/{id}")
    R<OrdersDto> findOneById(@PathVariable("id") Long id)
    {
        log.info("查看详细订单，拿到订单id为：{}",id);
        //根据订单的id拿到订单及其详细信息
        OrdersDto ordersDto = ordersService.getOneWithDeatils(id);
        //返回对象
        return R.success(ordersDto);
    }

    /**
     * 管理端，修改订单状态
     * @param orders
     * @return
     */
    @PutMapping()
    R<String> editOrderStatus(@RequestBody Orders orders)
    {
        log.info("管理端修改订单状态，拿到参数{}",orders.toString());
        //拿到实体，修改内容
        boolean b = ordersService.updateById(orders);
        if (b)
        {
            return R.success("修改状态成功");
        }
        return R.error("修改状态失败");
    }

    //———————————————————————————————以下是移动的接口—————————————————————————————————————————————————————

    /**
     * 移动端页面订单分页,给user和order使用
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    R<Page> orderPaging_User(Long page,Long pageSize)
    {
        //创建Page对象
        Page<Orders> ordersPage=new Page<>(page,pageSize);
        Page<OrdersDto> ordersDtoPage=new Page<>();
        //拿到用户id
        Long currentId = BaseContext.getCurrentId();
        log.info("user用户id为{}",currentId);
        //拿到当前用户的订单信息
        LambdaQueryWrapper<Orders> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId,currentId);
        //排序
        queryWrapper.orderByDesc(Orders::getOrderTime);
        ordersService.page(ordersPage,queryWrapper);
        //拷贝基本信息
        BeanUtils.copyProperties(ordersPage,ordersDtoPage,"records");
        //装填ordersDtoPage的records
        List<Orders> records = ordersPage.getRecords();
        //使用stream的API，封装DTO的内部数据
        List<OrdersDto> records_Dto = records.stream().map(one -> {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(one, ordersDto);
            //补充新增属性
            LambdaQueryWrapper<OrderDetail> queryWrapper1 = new LambdaQueryWrapper<>();
            //条件是，属于one的订单号的菜品
            queryWrapper1.eq(OrderDetail::getOrderId, one.getId());
            List<OrderDetail> list = orderDetailService.list(queryWrapper1);
            ordersDto.setOrderDetails(list);
            return ordersDto;
        }).collect(Collectors.toList());
        //将records_Dto封装到PageDto中
        ordersDtoPage.setRecords(records_Dto);
        log.info("封装出来的订单records是{}",records_Dto);
        return R.success(ordersDtoPage);
    }

    /**
     * 提交购物车订单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    R<String> submitOrder(@RequestBody Orders orders)
    {
        log.info("购物车提交的菜品等为{}",orders.toString());
        //封装操作，提交订单，订单详情，清空购物车
        ordersService.submit(orders);
        return R.success("提交成功");
    }

    /**
     * 再来一单 返回首页
     * @param orders
     * @return
     */
    @PostMapping("/again")
    R<String> LastOrderAgain(@RequestBody Orders orders)
    {
        log.info("上一单的单号是{}",orders.getId());
        return R.success("再来一单");
    }


}
