package com.practice.reggie.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {
    /**
     * 分页插件旧版 3.5.x以下,导入这个插件，Page对象才能生效，不导入不会生效也不会报错
     * @return
     */
    @Bean
    public PaginationInterceptor paginationInterceptor()
    {
        return new PaginationInterceptor();
    }
}
