package com.practice.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.practice.reggie.mapper.orderDetailMapper;
import com.practice.reggie.model.OrderDetail;
import com.practice.reggie.service.orderDetailService;
import org.springframework.stereotype.Service;

@Service
public class orderDetailServiceImpl extends ServiceImpl<orderDetailMapper, OrderDetail> implements orderDetailService{
}
