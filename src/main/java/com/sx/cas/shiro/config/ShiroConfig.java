package com.sx.cas.shiro.config;

import com.sx.cas.shiro.realm.MyCasRealm;
import com.sx.cas.shiro.realm.MyRealm;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.cas.CasFilter;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.session.SingleSignOutHttpSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlProcessor;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.PathResource;
import org.springframework.web.filter.DelegatingFilterProxy;

import javax.servlet.Filter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * shiro config
 * Created by Administrator on 2017/10/23 0023.
 */
@Configuration
public class ShiroConfig {

    private  Logger logger = LoggerFactory.getLogger(getClass());

    // cas server地址
    public static  String casServerUrlPrefix = "";
    // Cas登录页面地址
    public static  String casLoginUrl = "";
    // Cas登出页面地址
    public static  String casLogoutUrl = "";
    // 当前工程对外提供的服务地址
    public static  String shiroServerUrlPrefix = "";
    // casFilter UrlPattern
    public static  String casFilterUrlPattern = "";
    // 登录地址
    public static  String loginUrl = "";
    // 登出地址（casserver启用service跳转功能，需在webapps\cas\WEB-INF\cas.properties文件中启用cas.logout.followServiceRedirects=true）
    public static  String logoutUrl = "";
    // 登录成功地址
    public static  String loginSuccessUrl = "";
    // 权限认证失败跳转地址
    public static  String unauthorizedUrl = "";


    @Bean("propertyConfig")
    public YamlPropertiesFactoryBean getYml(){
        YamlPropertiesFactoryBean yp = new YamlPropertiesFactoryBean();
        yp.setResources(new ClassPathResource("application.yml"));
        yp.setResolutionMethod(YamlProcessor.ResolutionMethod.FIRST_FOUND);
        Properties pro = yp.getObject();
        logger.info(pro.toString());
        logger.info("yml config is {} {} {}",
                pro.stringPropertyNames().stream()
                    .map(key->{
                        String val = pro.getProperty(key);
                        logger.info("key is {} value is {}",key,val);
                        return val;
                    }).collect(Collectors.toList()).toArray()
        );

        casServerUrlPrefix = pro.getProperty("cas.prefix");
        casLoginUrl = casServerUrlPrefix + pro.getProperty("cas.login");
        casLogoutUrl = casServerUrlPrefix + pro.getProperty("cas.logout");
        shiroServerUrlPrefix = pro.getProperty("server.prefix");
        casFilterUrlPattern = pro.getProperty("server.urlPattern");
        loginSuccessUrl = pro.getProperty("server.index");
        unauthorizedUrl = pro.getProperty("server.unauthorizedUrl");

        loginUrl = casLoginUrl + "?service=" + shiroServerUrlPrefix + casFilterUrlPattern;
        logoutUrl = casLogoutUrl+"?service="+shiroServerUrlPrefix;

        return yp;
    }

    @Bean
    public EhCacheManager getEhCacheManager(){
        EhCacheManager em = new EhCacheManager();
        em.setCacheManagerConfigFile("classpath:ehcache-shiro.xml");
        return em;
    }

    @Bean
    public FilterRegistrationBean delegatingFilterProxy(){
        FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
        filterRegistration.setFilter(new DelegatingFilterProxy("shiroFilter"));
        //  该值缺省为false,表示生命周期由SpringApplicationContext管理,设置为true则表示由ServletContainer管理
        filterRegistration.addInitParameter("targetFilterLifecycle", "true");
        filterRegistration.setEnabled(true);
        filterRegistration.addUrlPatterns("/*");
        return filterRegistration;
    }

    @Bean(name = "lifecycleBeanPostProcessor")
    public LifecycleBeanPostProcessor getLifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    @Bean
    public MyRealm getRealm(){
        return new MyRealm();
    }

    @Bean
    public MyCasRealm getCasRealm(){
        return new MyCasRealm();
    }

    @Bean
    public DefaultWebSecurityManager getDefaultWebSecurityManager(MyCasRealm realm){
        DefaultWebSecurityManager dwsm = new DefaultWebSecurityManager();
        dwsm.setRealm(realm);
        //      <!-- 用户授权/认证信息Cache, 采用EhCache 缓存 -->
        dwsm.setCacheManager(getEhCacheManager());
        // 指定 SubjectFactory
        //dwsm.setSubjectFactory(new CasSubjectFactory());
        return dwsm;
    }

    /**
     * 注册单点登出listener
     * @return
     */
    @Bean
    public ServletListenerRegistrationBean singleSignOutHttpSessionListener(){
        ServletListenerRegistrationBean bean = new ServletListenerRegistrationBean();
        bean.setListener(new SingleSignOutHttpSessionListener());
//        bean.setName(""); //默认为bean name
        bean.setEnabled(true);
        //bean.setOrder(Ordered.HIGHEST_PRECEDENCE); //设置优先级
        return bean;
    }

    /**
     * 注册单点登出filter
     * @return
     */
    @Bean
    public FilterRegistrationBean singleSignOutFilter(){
        FilterRegistrationBean bean = new FilterRegistrationBean();
        bean.setName("singleSignOutFilter");
        bean.setFilter(new SingleSignOutFilter());
        bean.addUrlPatterns("/*");
        bean.setEnabled(true);
        //bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

    @Bean
    public CasFilter getCasFilter(){
        CasFilter casFilter = new CasFilter();
        casFilter.setName("casFilter");
        casFilter.setEnabled(true);
        // 登录失败后跳转的URL，也就是 Shiro 执行 CasRealm 的 doGetAuthenticationInfo 方法向CasServer验证tiket
        casFilter.setFailureUrl(loginUrl);// 我们选择认证失败后再打开登录页面
        return casFilter;
    }

    @Bean(name = "shiroFilter")
    public ShiroFilterFactoryBean getShiroFilterFactoryBean(DefaultWebSecurityManager securityManager, CasFilter casFilter,Properties propertyConfig) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        // 必须设置 SecurityManager
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        // 如果不设置默认会自动寻找Web工程根目录下的"/login.jsp"页面
        shiroFilterFactoryBean.setLoginUrl(loginUrl);
        // 登录成功后要跳转的连接
        shiroFilterFactoryBean.setSuccessUrl(loginSuccessUrl);
        shiroFilterFactoryBean.setUnauthorizedUrl(unauthorizedUrl);
        // 添加casFilter到shiroFilter中
        Map<String, Filter> filters = new HashMap<>();
        filters.put("casFilter", casFilter);
        //filters.put("logout",logoutFilter());
        shiroFilterFactoryBean.setFilters(filters);

        loadShiroFilterChain(shiroFilterFactoryBean);
        return shiroFilterFactoryBean;
    }

    private void loadShiroFilterChain(ShiroFilterFactoryBean shiroFilterFactoryBean){
        /////////////////////// 下面这些规则配置最好配置到配置文件中 ///////////////////////
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<String, String>();

        // authc：该过滤器下的页面必须登录后才能访问，它是Shiro内置的一个拦截器org.apache.shiro.web.filter.authc.FormAuthenticationFilter
        // anon: 可以理解为不拦截
        // user: 登录了就不拦截
        // roles["admin"] 用户拥有admin角色
        // perms["permission1"] 用户拥有permission1权限
        // filter顺序按照定义顺序匹配，匹配到就验证，验证完毕结束。
        // url匹配通配符支持：? * **,分别表示匹配1个，匹配0-n个（不含子路径），匹配下级所有路径

        //1.shiro集成cas后，首先添加该规则
        filterChainDefinitionMap.put(casFilterUrlPattern, "casFilter");
        //filterChainDefinitionMap.put("/logout","logout"); //logut请求采用logout filter
        filterChainDefinitionMap.put("/hello","authc");
        //2.不拦截的请求
        filterChainDefinitionMap.put("/login/**","anon");
        filterChainDefinitionMap.put("/css*//**","anon");
         filterChainDefinitionMap.put("/js*//**","anon");
         filterChainDefinitionMap.put("/login", "anon");
         filterChainDefinitionMap.put("/logout","anon");
         filterChainDefinitionMap.put("/error","anon");
         //3.拦截的请求（从本地数据库获取或者从casserver获取(webservice,http等远程方式)，看你的角色权限配置在哪里）
         filterChainDefinitionMap.put("*//**", "authc");
         filterChainDefinitionMap.put("/user", "authc"); //需要登录
         filterChainDefinitionMap.put("/user/add*//**", "authc,roles[admin]"); //需要登录，且用户角色为admin
         filterChainDefinitionMap.put("/user/delete*//**", "authc,perms[\"user:delete\"]"); //需要登录，且用户有权限为user:delete

        //4.登录过的不拦截
        filterChainDefinitionMap.put("/**", "user");

        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
    }


    public static void main(String[] args) {
        YamlPropertiesFactoryBean yp = new YamlPropertiesFactoryBean();
        yp.setResources(new PathResource("application.yml"));
        yp.setResolutionMethod(YamlProcessor.ResolutionMethod.FIRST_FOUND);
        Properties pro = yp.getObject();
        System.out.println(pro);
    }
}
