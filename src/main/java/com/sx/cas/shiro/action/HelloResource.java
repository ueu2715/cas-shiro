package com.sx.cas.shiro.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HelloResource {
    private final Logger log = LoggerFactory.getLogger(getClass());
    @RequestMapping("/hello")
    public String hello(Model model){
        log.info("in hello method");
        return "hello";
    }
}
