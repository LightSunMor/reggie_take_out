package com.practice.reggie;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@MapperScan("com.practice.reggie.mapper")
@Slf4j
@SpringBootApplication
@ServletComponentScan //用于扫描类似WebFilter的注解，这样过滤器才能生效
@EnableTransactionManagement //支持事务的开启
public class ReggieApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReggieApplication.class,args);
        log.info("项目启动成功.....");
    }
}
