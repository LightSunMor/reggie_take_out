package com.practice.reggie.Handler;

import com.practice.reggie.model.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 捕获全局异常  有因为employee表中username唯一，而再次新增的时候再次添加相同的username会报异常
 */
@ControllerAdvice(annotations = {RestController.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 一旦出现SQLIntegrityConstraintViolationException异常，就会被此方法处理
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException exception)
    {
        log.error(exception.getMessage());
        if (exception.getMessage().contains("Duplicate entry"))
        {
            String[] split = exception.getMessage().split(" ");
            String s = split[2] + "已存在";
            return R.error(s);

        }
        return R.error("未知错误");
    }

    /**
     * 捕获处理自定义业务异常类，显示在页面
     * @param exception
     * @return
     */
    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException exception)
    {
        log.error(exception.getMessage());
        return R.error(exception.getMessage());
    }

    //如果不需要返回json数据，而要渲染某个页面模板返回给浏览器，那么可以这么实现：
//    @ExceptionHandler(value = Exception.class)
//    public ModelAndView myErrorHandler(Exception ex) {
//        ModelAndView modelAndView = new ModelAndView();
//        //指定错误页面的模板页(error),每次报错都转发到这个页面
//        modelAndView.setViewName("error");
//        modelAndView.addObject("code", ex.getMessage());
//        modelAndView.addObject("msg", ex.getCause());
//        return modelAndView;
//    }

}
