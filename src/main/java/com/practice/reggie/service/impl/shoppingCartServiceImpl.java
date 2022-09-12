package com.practice.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.practice.reggie.mapper.shoppingCartMapper;
import com.practice.reggie.model.ShoppingCart;
import com.practice.reggie.service.shoppingCartService;
import org.springframework.stereotype.Service;

@Service
public class shoppingCartServiceImpl extends ServiceImpl<shoppingCartMapper, ShoppingCart> implements shoppingCartService {
}
