package com.example.shiro;

import com.example.aop.CrosFilter;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.PasswordMatcher;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.apache.shiro.mgt.SecurityManager;

import javax.servlet.Filter;
import java.util.*;

/**
 * Created by dugq on 2017/10/26.
 */
public class ShiroConfig {


    @Bean(name="shiroFilter")
    public ShiroFilterFactoryBean shiroFilter(@Qualifier("securityManager") SecurityManager manager) {
        ShiroFilterFactoryBean bean=new ShiroFilterFactoryBean();
        bean.setSecurityManager(manager);
        //配置登录的url和登录成功的url
        bean.setLoginUrl("/template/login.html");
        bean.setSuccessUrl("/home");
        //配置访问权限
        LinkedHashMap<String, String> filterChainDefinitionMap=new LinkedHashMap<>();
        filterChainDefinitionMap.put("/template/login.html", "anon"); //表示可以匿名访问
        filterChainDefinitionMap.put("/management/**", "anon"); //表示可以匿名访问
        filterChainDefinitionMap.put("/user/login", "anon"); //表示可以匿名访问
        filterChainDefinitionMap.put("/user/login4ajax", "anon"); //表示可以匿名访问
        filterChainDefinitionMap.put("/mystatic/**", "anon");
        filterChainDefinitionMap.put("/template/*", "myAuthc,mPms"); //表示可以匿名访问
        filterChainDefinitionMap.put("/**", "myAuthc");//表示需要认证才可以访问
        filterChainDefinitionMap.put("/user/logout", "logout");
        filterChainDefinitionMap.put("/testShiro/*", "myAuthc,mPms");
        bean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        MyPermissionsAuthorizationFilter mPms = myPermissionsAuthorizationFilter();
        Map<String, Filter> map = new HashMap();
        MyFormAuthenticationFilter myAnon = new MyFormAuthenticationFilter();
        map.put("myAuthc", myAnon);
        map.put("mPms",mPms);
        bean.setFilters(map);
        return bean;
    }

    public MyPermissionsAuthorizationFilter  myPermissionsAuthorizationFilter(){
        return new MyPermissionsAuthorizationFilter();
    }


    //配置核心安全事务管理器
    @Bean(name="securityManager")
    public SecurityManager securityManager(@Qualifier("authRealm") AuthRealm authRealm) {
        System.err.println("--------------shiro已经加载----------------");
        DefaultWebSecurityManager manager=new DefaultWebSecurityManager();
//        manager.setRealms();
        manager.setRealm(authRealm);
        manager.setSessionManager(getDefaultWebSessionManager());
        return manager;
    }


    //配置自定义的权限登录器
    @Bean(name="authRealm")
    public AuthRealm authRealm(@Qualifier("credentialsMatcher") CredentialsMatcher matcher) {
        AuthRealm authRealm=new AuthRealm();
        authRealm.setCredentialsMatcher(matcher);
        return authRealm;
    }
    //配置自定义的密码比较器
    @Bean(name="credentialsMatcher")
    public CredentialsMatcher credentialsMatcher(@Qualifier("passwordService") PasswordService passwordService) {
        PasswordMatcher passwordMatcher = new PasswordMatcher();
        passwordMatcher.setPasswordService(passwordService);
        return passwordMatcher;
    }
    @Bean(name = "passwordService")
    public PasswordService passwordService(){
       return new DefaultPasswordService(){
           @Override
           public boolean passwordsMatch(Object submittedPlaintext, String saved) {
               if(Objects.isNull(submittedPlaintext)){
                   return false;
               }
               if (submittedPlaintext instanceof String){
                   return submittedPlaintext.equals(saved);
               }else if(submittedPlaintext instanceof char[]){
                   char[] text = (char[])submittedPlaintext;
                   return saved.equals(new String(text));
               }
               return super.passwordsMatch(submittedPlaintext,saved);
           }
       };
    }

    @Bean("sessionDAO")
    public SessionDAO getSessionDAO(){
        return new EnterpriseCacheSessionDAO();
    }


    @Bean("sessionManager")
    public SessionManager getDefaultWebSessionManager() {
        SessionManager sessionManager = new SessionManager();
        // 设置全局session过期时间 /毫秒 半小时
        sessionManager.setGlobalSessionTimeout(1800000);
        sessionManager.setDeleteInvalidSessions(true);
        sessionManager.setSessionValidationSchedulerEnabled(true);
        sessionManager.setSessionIdCookieEnabled(true);
        // 设置监听器,自定义的监听器也要加在这里
        List list = new ArrayList();
        sessionManager.setSessionListeners(list);
        sessionManager.setSessionDAO(getSessionDAO());
        return sessionManager;
    }
    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor(){
        return new LifecycleBeanPostProcessor();
    }
    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator(){
        DefaultAdvisorAutoProxyCreator creator=new DefaultAdvisorAutoProxyCreator();
        creator.setProxyTargetClass(true);
        return creator;
    }
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(@Qualifier("securityManager") SecurityManager manager) {
        AuthorizationAttributeSourceAdvisor advisor=new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(manager);
        return advisor;
    }
}
