package com.practice.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.practice.reggie.model.Category;
import org.springframework.stereotype.Service;
@Service
public interface categoryService extends IService<Category> {

    public boolean removeById(Long id);
}
