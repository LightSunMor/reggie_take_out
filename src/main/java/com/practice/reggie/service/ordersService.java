package com.practice.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.practice.reggie.model.Orders;
import com.practice.reggie.model.dto.OrdersDto;
import org.springframework.stereotype.Service;

@Service
public interface ordersService extends IService<Orders> {
    void submit(Orders orders);

    OrdersDto getOneWithDeatils(Long id);
}
