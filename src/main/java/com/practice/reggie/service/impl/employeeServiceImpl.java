package com.practice.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.practice.reggie.mapper.employeeMapper;
import com.practice.reggie.model.Employee;
import com.practice.reggie.service.employeeService;
import org.springframework.stereotype.Service;

@Service
public class employeeServiceImpl extends ServiceImpl<employeeMapper, Employee> implements employeeService {
}
