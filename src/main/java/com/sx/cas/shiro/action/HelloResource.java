package com.sx.cas.shiro.action;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
public class HelloResource {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @RequestMapping("/hello")
    public String hello(ModelMap model, HttpServletRequest request){
        log.info("in hello method");
        System.out.println(request.getUserPrincipal().getName());

        PrincipalCollection p = SecurityUtils.getSubject().getPrincipals();
        p.asList().stream()
                .filter(s->s.getClass() == java.util.HashMap.class)
                .forEach(m->{
                    Map<String,String> mm = (Map)m;
                    System.out.println(mm);
                    model.put("map",mm);
                    mm.forEach((k,v)->{
                        model.addAllAttributes(mm);
                        System.out.println(k+":"+v);
                    });
                });
        model.put("user",SecurityUtils.getSubject().getPrincipal());
        return "hello";
    }
}
