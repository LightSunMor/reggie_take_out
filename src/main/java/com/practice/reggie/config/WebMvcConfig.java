package com.practice.reggie.config;

import com.practice.reggie.Handler.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

@Configuration
@Slf4j
public class WebMvcConfig extends WebMvcConfigurationSupport {
    /**
     * 配置静态资源的映射 ，原本springboot默认的是访问resource下的static和template
     * @param registry
     *
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("进行静态资源自我配置映射");
        registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
        registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");
    }

    /**
     * 扩展mvc的消息转换器（json《-》java）
     * @param converters
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        //创建消息转换器对象
        MappingJackson2HttpMessageConverter messageConverter=new MappingJackson2HttpMessageConverter();
        //设置对象转换器，底层使用JacksonObjectMapper 将java 转为json
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        //将上面的消息转换器对象追加到mvc框架的转换器集合中,且放在第一位
        converters.add(0,messageConverter);

    }
}
