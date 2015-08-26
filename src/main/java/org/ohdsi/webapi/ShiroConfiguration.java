package org.ohdsi.webapi;

import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.PasswordMatcher;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.ohdsi.webapi.shiro.SampleRealm;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Created by GMalikov on 20.08.2015.
 */

@Configuration
public class ShiroConfiguration {

    @Bean
    public ShiroFilterFactoryBean shiroFilter(){
        ShiroFilterFactoryBean factoryBean = new ShiroFilterFactoryBean();
        factoryBean.setSecurityManager(securityManager());
        return factoryBean;
    }

    @Bean(name = "securityManager")
    public DefaultWebSecurityManager securityManager(){
        final DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(realm());
        return securityManager;
    }


    @Bean
    public DefaultWebSessionManager sessionManager(){
        final DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        return sessionManager;
    }

    @Bean(name = "realm")
    @DependsOn("lifecycleBeanPostProcessor")
    public SampleRealm realm(){
        final SampleRealm realm = new SampleRealm();
        realm.setCredentialsMatcher(credentialsMatcher());
        return realm;
    }

    @Bean(name = "credentialsMatcher")
    public PasswordMatcher credentialsMatcher(){
        final PasswordMatcher credentialsMatcher = new PasswordMatcher();
        credentialsMatcher.setPasswordService(passwordService());
        return credentialsMatcher;
    }

    @Bean(name = "passwordService")
    public DefaultPasswordService passwordService(){
        return new DefaultPasswordService();
    }

    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor(){
        return new LifecycleBeanPostProcessor();
    }
}
