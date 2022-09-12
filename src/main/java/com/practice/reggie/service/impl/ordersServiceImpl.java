package com.practice.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.practice.reggie.Handler.CustomException;
import com.practice.reggie.mapper.ordersMapper;
import com.practice.reggie.model.*;
import com.practice.reggie.model.dto.OrdersDto;
import com.practice.reggie.service.ordersService;
import com.practice.reggie.util.BaseContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class ordersServiceImpl extends ServiceImpl<ordersMapper, Orders> implements ordersService {
    @Autowired
    private shoppingCartServiceImpl shoppingCartService;
    @Autowired
    private userServiceImpl userService;
    @Autowired
    private addressBookServiceImpl addressBookService;
    @Autowired
    private orderDetailServiceImpl orderDetailService;

    /**
     * 订单下单提交数据
     * @param orders
     */
    @Override
    @Transactional
    public void submit(Orders orders) {
        //当前用户
        Long id = BaseContext.getCurrentId();
        //购物车中菜品或者套餐
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,id);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        if (list.size()==0)
        {
            throw new CustomException("购物车为空，下单出错");
        }
        //查询用户数据
        User user = userService.getById(id);
        //查询地址
        AddressBook book = addressBookService.getById(orders.getAddressBookId());
        if (book==null)
        {
            throw new CustomException("地址信息有误，不能下单");
        }
        //向订单表插入数据，一条
        long orderId = IdWorker.getId(); //IdWorker生成id
        orders.setId(orderId); //本来可以自己生成，但在插入明细表的时候也要用，为保证数据统一性，就手动添加id
        orders.setNumber(String.valueOf(orderId));
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        //用于计算总金额，可累加，初始为0
        //该对象是属于原子操作，不可分割，在多线程下也不会出错
        AtomicInteger amount=new AtomicInteger(0);
        //金额,要自己算
        List<OrderDetail> orderDetails = list.stream().map(item -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());//单份菜品或套餐的金额
            //算出每遍历一种菜品或套餐的金额和
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());
        orders.setAmount(new BigDecimal(amount.get()));
        orders.setUserId(id);
        orders.setUserName(user.getName());
        orders.setConsignee(book.getConsignee());
        orders.setPhone(book.getPhone());
        //拼接地址
        orders.setAddress((book.getProvinceName()==null?"":book.getProvinceName())
                +(book.getCityName()==null?"":book.getCityName())
                +(book.getDistrictName()==null?"":book.getDistrictName())
                +(book.getDetail()==null?"":book.getDetail()));

        super.save(orders);
        //向订单明细表插入数据，可能多条
        orderDetailService.saveBatch(orderDetails);
        //清空购物车
        shoppingCartService.remove(queryWrapper);
    }

    /**
     * 根据订单号拿到订单和详细信息，管理端
     * @param id
     * @return
     */
    @Override
    public OrdersDto getOneWithDeatils(Long id) {
        //先查出Orders
        Orders orders = super.getById(id);
        OrdersDto ordersDto = new OrdersDto();
        //拷贝
        BeanUtils.copyProperties(orders,ordersDto);
        //手动添加新属性
        LambdaQueryWrapper<OrderDetail> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId,id);
        List<OrderDetail> list = orderDetailService.list(queryWrapper);
        ordersDto.setOrderDetails(list);
        return ordersDto;
    }
}
