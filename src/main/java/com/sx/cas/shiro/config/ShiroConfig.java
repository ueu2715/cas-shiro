package com.sx.cas.shiro.config;

import com.sx.cas.shiro.realm.MyRealm;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.DelegatingFilterProxy;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * shiro config
 * Created by Administrator on 2017/10/23 0023.
 */
@Configuration
public class ShiroConfig {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String loginUrl = "/login.jsp";

    private static final String loginSuccessUrl = "/success.jsp";

    private static final String unauthorizedUrl = "/fail.jsp";

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
    public DefaultWebSecurityManager getDefaultWebSecurityManager(MyRealm realm){
        DefaultWebSecurityManager dwsm = new DefaultWebSecurityManager();
        dwsm.setRealm(realm);
        //      <!-- 用户授权/认证信息Cache, 采用EhCache 缓存 -->
        dwsm.setCacheManager(getEhCacheManager());
        // 指定 SubjectFactory
        //dwsm.setSubjectFactory(new CasSubjectFactory());
        return dwsm;
    }

    @Bean(name = "shiroFilter")
    public ShiroFilterFactoryBean getShiroFilterFactoryBean(DefaultWebSecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        // 必须设置 SecurityManager
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        // 如果不设置默认会自动寻找Web工程根目录下的"/login.jsp"页面
        shiroFilterFactoryBean.setLoginUrl(loginUrl);
        // 登录成功后要跳转的连接
        shiroFilterFactoryBean.setSuccessUrl(loginSuccessUrl);
        shiroFilterFactoryBean.setUnauthorizedUrl(unauthorizedUrl);
        // 添加casFilter到shiroFilter中
        //Map<String, Filter> filters = new HashMap<>();
        //filters.put("casFilter", casFilter);
        // filters.put("logout",logoutFilter());
        //shiroFilterFactoryBean.setFilters(filters);

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
        //filterChainDefinitionMap.put(casFilterUrlPattern, "casFilter");
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


}
