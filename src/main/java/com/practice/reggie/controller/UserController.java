package com.practice.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.practice.reggie.model.R;
import com.practice.reggie.model.User;
import com.practice.reggie.service.impl.userServiceImpl;
import com.practice.reggie.util.SMSUtils;
import com.practice.reggie.util.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private userServiceImpl userService;

    /**
     * user登录接口
     * @param map
     * @param request
     * @return
     */
    @PostMapping("/login")
    R<User> login_user(@RequestBody Map map, HttpServletRequest request) //可以使用Map集合接收，因为map也属于json写法,也可以新建Dto对象来接收
    {
        log.info("user登录时，拿到的参数{}",map.toString());
        //1.先从map中获取验证码和手机号
        String phone = (String) map.get("phone");
        String code = (String) map.get("code");
        //2.先判断验证码是否正确
        String code_login = (String) request.getSession().getAttribute(phone);
        if (code_login!=null&&code_login.equals(code))
        {
            //3.然后判断当前手机号是否之前就存在，如果存在，就登录
            LambdaQueryWrapper<User> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            User user=userService.getOne(queryWrapper);
            if (user==null)
            {
                //4.如果没有，就自动新建一个用户
                 user = new User();
                user.setPhone(phone);
                userService.save(user);
            }
            //5.将user的id存入session以躲过filter 的过滤
            request.getSession().setAttribute("user",user.getId());
            return R.success(user);  //需要给客户端也传输一份user保存起来
        }
       //6.验证失败，登录失败
        return R.error("用户登录失败");
    }

    String phone_mark=null;
    /**
     * 获取验证码，并发送短信验证接口（发送短信目前实现不了）
     * @param user
     * @param request
     * @return
     */
    @PostMapping("/sendMsg")
    R<String> sendMsg(@RequestBody User user, HttpServletRequest request) //此处必须用user接收，因为前端将参数封装成了json
    {
        String code_login=null;
        if (StringUtils.isNotEmpty(user.getPhone()))
        //生成随机的验证码,使用ValidateCodeUtils工具类,只能生成4位或6位
        {
            code_login= ValidateCodeUtils.generateValidateCode(6).toString();
            log.info("生成的验证码是{}",code_login);
            //调用阿里云的短信服务API完成发送短信
//        SMSUtils.sendMessage();
            //将生成的验证码保存到Session,使用手机号作为key做唯一表示
            request.getSession().setAttribute(user.getPhone(),code_login);
            phone_mark=user.getPhone();
            return R.success("短信发送成功");
        }
        return R.error("短信发送失败");
    }

    @PostMapping("/loginout")
    R<String> logout_User(HttpServletRequest request)
    {
        if (phone_mark!=null)
        {
            request.getSession().removeAttribute(phone_mark);
        }
        phone_mark=null;
        return R.success("用户退出登录成功");
    }

}
