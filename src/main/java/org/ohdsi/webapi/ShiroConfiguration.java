package org.ohdsi.webapi;

import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.PasswordMatcher;
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.ohdsi.webapi.shiro.SampleRealm;
import org.ohdsi.webapi.shiro.SampleShiroWaffleRealm;
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

//    @Bean(name = "realm")
//    @DependsOn("lifecycleBeanPostProcessor")
//    public SampleRealm realm(){
//        final SampleRealm realm = new SampleRealm();
//        realm.setCredentialsMatcher(new SimpleCredentialsMatcher());
//        return realm;
//    }

    @Bean(name = "realm")
    @DependsOn("lifecycleBeanPostProcessor")
    public SampleShiroWaffleRealm realm(){
        final SampleShiroWaffleRealm realm = new SampleShiroWaffleRealm();
        realm.setCredentialsMatcher(new SimpleCredentialsMatcher());
        return realm;
    }

    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor(){
        return new LifecycleBeanPostProcessor();
    }
}
