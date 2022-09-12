package com.practice.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.practice.reggie.mapper.dishFlavorMapper;
import com.practice.reggie.model.DishFlavor;
import com.practice.reggie.service.dishFlavorService;
import org.springframework.stereotype.Service;

@Service
public class dishFlavorServiceImpl extends ServiceImpl<dishFlavorMapper, DishFlavor> implements dishFlavorService {
}
