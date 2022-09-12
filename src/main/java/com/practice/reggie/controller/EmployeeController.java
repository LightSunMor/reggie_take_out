package com.practice.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.practice.reggie.model.Employee;
import com.practice.reggie.model.dto.MessagePage;
import com.practice.reggie.model.R;
import com.practice.reggie.service.impl.employeeServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private employeeServiceImpl employeeService;
    /**
     * 员工登录,按照要求分步处理
     * @param employee
     * @param request
     * @return
     */
    @PostMapping("/login")
    R<Employee> login_emp(@RequestBody Employee employee, HttpServletRequest request)
    {
        //1.将页面提交的密码进行md5加密
        String password = employee.getPassword();
        password= DigestUtils.md5DigestAsHex(password.getBytes());
        //2.根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee one = employeeService.getOne(queryWrapper);//因为数据库username是规定唯一的，所以可以使用getone

        //3.如果没有查询到则返回登录失败结果
        if (one==null)
        {
            return R.error("不存在该用户");
        }
        //4.密码比对，如果不一致则返回登录失败结果
        if (!one.getPassword().equals(password))
        {
            return R.error("密码错误");
        }
        //5.查看员工状态，如果为禁用的状态，就返回员工禁用状态结果
        if (one.getStatus()!=1)
        {
            return R.error("该账号不可用");
        }
        //6.登录成功，将员工id存入session并返回登录成功结果
        request.getSession().setAttribute("employee",one.getId());
        return R.success(one);
    }

    @PostMapping("/logout")
    R<String> logout_emp(HttpServletRequest request)
    {
        //清理当前员工id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 分页表格查询
     * 返回值可以自己定义一个类，也可以返回R<Page> ，因为Page类中有total和records
     * @param pageSize
     * @param page
     * @param name
     * @return
     */
    @GetMapping("/page")
    R<MessagePage<Employee>> findByPage_emp(@RequestParam("pageSize") String pageSize,@RequestParam("page")String page,String name)//可加可不加requestparam（requered=false）
    {
        log.info("员工分页拿到的page:{},pageSize:{},name:{}",page,pageSize,name);
        List<Employee> records=null;
        long pages=0;
        long total=0;
        Page<Employee> employeePage=null;
        //封装page对象
        Page<Employee> PageObj = new Page<Employee>(Long.parseLong(page), Long.parseLong(pageSize));
        //1.先判断搜索框是否有搜索信息
        if (name==null)
        {
            //2.搜索框没有搜索信息
            employeePage = employeeService.page(PageObj);
        }
        else {
            //3.搜索框有搜素信息
            LambdaQueryWrapper<Employee> queryWrapper=new LambdaQueryWrapper<>();
            //添加过滤条件
            queryWrapper.like(Employee::getName,name);
            //    Children like(boolean condition, R column, Object val)  也可以使用这种方法，在其中传入一个条件，条件成立时，才执行当下的偶作: like(StringUtils.isNotEmpty(name),Employ::getName,name)
            //添加排序条件
            queryWrapper.orderByDesc(Employee::getUpdateTime);
            employeePage = employeeService.page(PageObj, queryWrapper);
        }
        //输出分页页数
        pages= employeePage.getPages();
        total= employeePage.getTotal();
        log.info("此次分页的总页数为{}",pages);
        log.info("此次查询总条数为{}",total);
        records= employeePage.getRecords();

        MessagePage<Employee> messagePage = new MessagePage<>();
        messagePage.setRecords(records);
        messagePage.setTotal(total);
        return R.success(messagePage);
    }

    /**
     * 添加员工
     * @param employee
     * @return
     */
    @PostMapping("/add")
    R<String> add_emp(@RequestBody Employee employee,HttpServletRequest request) {
        //1.拿到登录者的id，装配employee

//        long login_id = (long) request.getSession().getAttribute("employee");
//        employee.setCreateTime(LocalDateTime.now()); //使用了自动填充功能
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(login_id);
//        employee.setCreateUser(login_id);

        //2.将employee的密码进行md5加密
        String password="123456";
        password=DigestUtils.md5DigestAsHex(password.getBytes());
        employee.setPassword(password);
        //3.存入对象
        System.out.println(employee.toString());
        boolean b = employeeService.save(employee);
        if (b)
        {
            return R.success("添加"+employee.getUsername()+"成功");
        }
        return R.error("添加失败");
    }

    /**
     * 根据出传入的数据根据id进行修改  updateById
     * 修改员工信息，禁用启用状态按钮
     * @param employee
     * @param request
     * @return
     */
    @PutMapping
    R<String> update_emp(@RequestBody Employee employee,HttpServletRequest request)
    {
        //1.拿到当前登录的人的id，作为createUser
//        Long login_user = (Long) request.getSession().getAttribute("employee");
//        employee.setUpdateUser(login_user);
//        employee.setUpdateTime(LocalDateTime.now());
        //2.根据id修改员工信息
        boolean b = employeeService.updateById(employee);
        if (b)
        {
            return R.success("修改成功");
        }
        return R.error("修改失败");
    }

    /**
     * 根据id查询员工的信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    R<Employee> selectOne_emp(@PathVariable("id") String id)
    {
        //1.拿到id，并转化为long型
        long id_long;
        id_long=Long.parseLong(id);
        //2.查询员工
        LambdaQueryWrapper<Employee> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getId,id);
        Employee one = employeeService.getOne(queryWrapper);//getById方法也可
        //3.返回信息
        return one!=null?R.success(one):R.error("该用户不存在了");
    }
}
