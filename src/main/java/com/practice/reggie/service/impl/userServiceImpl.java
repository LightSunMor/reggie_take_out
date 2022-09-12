package com.practice.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.practice.reggie.mapper.userMapper;
import com.practice.reggie.model.User;
import com.practice.reggie.service.userService;
import org.springframework.stereotype.Service;

@Service
public class userServiceImpl extends ServiceImpl<userMapper, User> implements userService {
}
