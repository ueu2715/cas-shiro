package com.sx.cas.shiro.config;

import com.sx.cas.shiro.filter.MyFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.ServletException;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<MyFilter> getFilter() throws ServletException {
        FilterRegistrationBean<MyFilter> frb = new FilterRegistrationBean<>();
        MyFilter mf = new MyFilter();
        frb.setFilter(mf);
        frb.addUrlPatterns("/*");
        frb.setName("myfilter");
        frb.setEnabled(true);
        return frb;
    }
}
