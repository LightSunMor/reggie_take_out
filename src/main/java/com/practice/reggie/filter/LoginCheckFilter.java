package com.practice.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.practice.reggie.model.R;
import com.practice.reggie.util.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Slf4j
@WebFilter(filterName = "LoginCheckFilter",urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    public void init(FilterConfig config) throws ServletException {
      log.info("登录过滤器已开启");
    }

    public void destroy() {
        log.info("登录过滤器已摧毁");
    }
    //路径匹配器，适应通配符
    /*
    *  作用：在项目中主要用来做路径的匹配，在权限模块会用到接口路径的匹配。
        用法规则：
           ？匹配一个字符
           * 匹配零个或多个字符
           ** 匹配路径中零个或多个目录
    * */
    public static final AntPathMatcher PATH_MATCHER=new AntPathMatcher();
    //判断当前请求是否需要拦截处理
    public boolean check(String[] urls,String requestURI)
    {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match==true){
                return true;
            }
        }
        return false;
    }
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest request1 = (HttpServletRequest) request;
        HttpServletResponse response1 = (HttpServletResponse) response;
        //1.获取本次请求的URI（不包括前面http和端口那一堆）
        log.info("拦截到请求：{}",request1.getRequestURI());
        String requestURI = request1.getRequestURI();
        //不需要处理的路径
        String[] urls=new String[]{
                "/employee/login","/employee/logout","/backend/**","/front/**","/user/sendMsg","/user/login"
        };
        //2.判断本次请求是否需要处理 ！！ 此思想易忽略
        boolean check = check(urls, requestURI);

        //3.如果check是true,不处理直接放行
        if (check)
        {
            log.info("{}请求不需要处理",requestURI);
            chain.doFilter(request1, response1);
            return;
        }
        //4-employee.判断登录状态，如果已登录，放行,通过输出流的方式向客户响应数据
        Object employee = request1.getSession().getAttribute("employee");
        if (employee!=null)
        {
            //使用BaseContext工具类在ThreadLocal中装入id
            long employee1= (long) employee;
            log.info("存入ThreadLocal的数据employee的id为：{}",employee1);
            BaseContext.setCurrentId(employee1);
            chain.doFilter(request1,response1);
            return;
        }
        //4-user.判断登录状态，如果已登录，放行,通过输出流的方式向客户响应数据
        Object user = request1.getSession().getAttribute("user");
        if (user!=null)
        {
            //使用BaseContext工具类在ThreadLocal中装入id
            long user1= (long) user;
            log.info("存入ThreadLocal的数据user的id为：{}",user1);
            BaseContext.setCurrentId(user1);
            chain.doFilter(request1,response1);
            return;
        }
        //5.未登录返回，未登录结果,写回错误信息到登录界面，异步请求不需要转发回到登录页面
        log.info("没有用户登录，已返回登录界面");
        response1.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));

        return;

    }
}
