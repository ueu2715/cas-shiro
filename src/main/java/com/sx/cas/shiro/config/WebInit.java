package com.sx.cas.shiro.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

public class WebInit implements WebApplicationInitializer{

    public void onStartup(@NotNull ServletContext servletContext) throws ServletException {
        AnnotationConfigWebApplicationContext cxt = new AnnotationConfigWebApplicationContext();
        cxt.setServletContext(servletContext);
        cxt.register(WebConfig.class);
        cxt.refresh();

        // Create DispatcherServlet
        DispatcherServlet servlet = new DispatcherServlet(cxt);
        // Register and map the Servlet
        ServletRegistration.Dynamic registration = servletContext.addServlet("app", servlet);
        registration.setLoadOnStartup(1);
        registration.addMapping("/");

        FilterRegistrationBean frb = cxt.getBean(FilterRegistrationBean.class);
        frb.onStartup(servletContext);
    }
}
