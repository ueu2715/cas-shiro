package com.sx.cas.shiro.login;

import com.sx.cas.shiro.bean.User;
import com.sx.cas.shiro.config.ShiroConfig;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.inject.Inject;
import java.util.Properties;

/**
 * Created by Administrator on 2017/10/23 0023.
 */
@Controller
public class LoginController {
    @Inject
    private Properties propertyConfig;

    @RequestMapping("login/{username}/{password}")
    public ModelAndView login(@PathVariable("username") String username, @PathVariable("password") String password) {
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(token);
        } catch (IncorrectCredentialsException ice) {
            // 捕获密码错误异常
            ModelAndView mv = new ModelAndView("fail");
            mv.addObject("message", "password error!");
            return mv;
        } catch (UnknownAccountException uae) {
            // 捕获未知用户名异常
            ModelAndView mv = new ModelAndView("fail");
            mv.addObject("message", "username error!");
            return mv;
        } catch (ExcessiveAttemptsException eae) {
            // 捕获错误登录过多的异常
            ModelAndView mv = new ModelAndView("fail");
            mv.addObject("message", "times error");
            return mv;
        }
        User user = new User(username,password);
        user.setId(1L);
        subject.getSession().setAttribute("user", user);
        return new ModelAndView("success");
    }

    @RequestMapping("/logout")
    public String logout(){
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
        return "redirect:" + ShiroConfig.logoutUrl + "?service="+ShiroConfig.loginUrl;
    }
}
