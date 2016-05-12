package org.ohdsi.webapi;

import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.*;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.ohdsi.webapi.shiro.SimpleAuthRealm;
import org.ohdsi.webapi.shiro.WindowsAuthRealm;

/**
 * Created by GMalikov on 20.08.2015.
 */

@Configuration
public class ShiroConfiguration {

    @Bean
    public ShiroFilterFactoryBean shiroFilter(){
        ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();
        shiroFilter.setSecurityManager(securityManager());
        return shiroFilter;
    }

    @Bean(name = "securityManager")
    public DefaultWebSecurityManager securityManager(){
        final DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        List<Realm> realms = new ArrayList<>();
        realms.add(windowsAuthRealm());
        realms.add(basicAuthRealm());
        securityManager.setRealms(realms);
        return securityManager;
    }


    @Bean
    public DefaultWebSessionManager sessionManager(){
        final DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        return sessionManager;
    }
    
    @Bean(name = "basicAuthRealm")
    @DependsOn("lifecycleBeanPostProcessor")
    public SimpleAuthRealm basicAuthRealm(){
        final SimpleAuthRealm realm = new SimpleAuthRealm();
        realm.setCredentialsMatcher(new SimpleCredentialsMatcher());
        return realm;
    }

    @Bean(name = "windowsAuthRealm")
    @DependsOn("lifecycleBeanPostProcessor")
    public WindowsAuthRealm windowsAuthRealm(){
        final WindowsAuthRealm realm = new WindowsAuthRealm();
        realm.setCredentialsMatcher(new SimpleCredentialsMatcher());
        return realm;
    }

    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor(){
        return new LifecycleBeanPostProcessor();
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor() {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager());
        return advisor;
    }
}
