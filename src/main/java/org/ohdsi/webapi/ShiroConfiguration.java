package org.ohdsi.webapi;

import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.filter.authc.AnonymousFilter;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.apache.shiro.web.filter.authc.UserFilter;
import org.apache.shiro.web.filter.authz.RolesAuthorizationFilter;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.ohdsi.webapi.shiro.SampleRealm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.servlet.*;
import javax.servlet.Filter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by GMalikov on 20.08.2015.
 */

@Configuration
public class ShiroConfiguration {

    @Bean
    public ShiroFilterFactoryBean shiroFilter(){
        ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();
        shiroFilter.setSecurityManager(securityManager());
        shiroFilter.setLoginUrl("/test/login");
        Map<String, String> filterChain = new HashMap<>();
        filterChain.put("/test/info", "authcBasic");
        shiroFilter.setFilterChainDefinitionMap(filterChain);
        return shiroFilter;
    }

    @Bean(name = "securityManager")
    public DefaultWebSecurityManager securityManager(){
        final DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(simpleAuthRealm());
        return securityManager;
    }


    @Bean
    public DefaultWebSessionManager sessionManager(){
        final DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        return sessionManager;
    }

    @Bean(name = "simpleAuthRealm")
    @DependsOn("lifecycleBeanPostProcessor")
    public SampleRealm simpleAuthRealm(){
        final SampleRealm simpleAuthRealm = new SampleRealm();
        simpleAuthRealm.setCredentialsMatcher(new SimpleCredentialsMatcher());
        return simpleAuthRealm;
    }

//    @Bean(name = "realm")
//    @DependsOn("lifecycleBeanPostProcessor")
//    public SampleShiroWaffleRealm realm(){
//        final SampleShiroWaffleRealm realm = new SampleShiroWaffleRealm();
//        realm.setCredentialsMatcher(new SimpleCredentialsMatcher());
//        return realm;
//    }

    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor(){
        return new LifecycleBeanPostProcessor();
    }
}
