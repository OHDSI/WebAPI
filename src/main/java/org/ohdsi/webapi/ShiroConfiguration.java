package org.ohdsi.webapi;

import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;
import org.apache.shiro.realm.Realm;
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
import org.ohdsi.webapi.shiro.SampleShiroWaffleRealm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import waffle.shiro.negotiate.NegotiateAuthenticationFilter;

import javax.servlet.*;
import javax.servlet.Filter;
import java.util.*;

/**
 * Created by GMalikov on 20.08.2015.
 */

@Configuration
public class ShiroConfiguration {

    @Bean
    public ShiroFilterFactoryBean shiroFilter(){
        ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();
        shiroFilter.setSecurityManager(securityManager());
        Map<String, String> filterChain = new HashMap<>();
        Map<String, Filter> filters = new HashMap<>();
        filters.put("authBasic", new BasicHttpAuthenticationFilter());
        filterChain.put("/test/info", "authBasic");
        shiroFilter.setFilterChainDefinitionMap(filterChain);
        shiroFilter.setFilters(filters);
        return shiroFilter;
    }

    @Bean(name = "securityManager")
    public DefaultWebSecurityManager securityManager(){
        final DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        List<Realm> realms = new ArrayList<>();
        realms.add(sampleShiroWaffleRealm());
        realms.add(simpleAuthRealm());
        securityManager.setRealms(realms);
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

    @Bean(name = "sampleShiroWaffleRealm")
    @DependsOn("lifecycleBeanPostProcessor")
    public SampleShiroWaffleRealm sampleShiroWaffleRealm(){
        final SampleShiroWaffleRealm sampleShiroWaffleRealm = new SampleShiroWaffleRealm();
        sampleShiroWaffleRealm.setCredentialsMatcher(new SimpleCredentialsMatcher());
        return sampleShiroWaffleRealm;
    }

    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor(){
        return new LifecycleBeanPostProcessor();
    }
}
