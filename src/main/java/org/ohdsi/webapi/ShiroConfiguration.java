package org.ohdsi.webapi;

import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;
import org.ohdsi.webapi.shiro.management.DefaultSecurity;
import org.ohdsi.webapi.shiro.management.Security;
import org.springframework.context.annotation.Primary;

/**
 * Created by GMalikov on 20.08.2015.
 */

@Configuration
public class ShiroConfiguration {
    
  @Bean
  @Primary
  public Security security() {
    return new DefaultSecurity();
  }
    
  @Bean
  public ShiroFilterFactoryBean shiroFilter(){
    ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();
    shiroFilter.setSecurityManager(securityManager());

    Security security = security();
    shiroFilter.setFilters(security.getFilters());
    shiroFilter.setFilterChainDefinitionMap(security.getFilterChain());

    return shiroFilter;
  }

  @Bean(name = "securityManager")
  public DefaultWebSecurityManager securityManager(){
    final DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
    final Security security = security();

    securityManager.setAuthenticator(security.getAuthenticator());
    
    Set<Realm> realms = security.getRealms();
    if (realms != null && !realms.isEmpty())
      securityManager.setRealms(realms);

    return securityManager;
  }

  @Bean
  public DefaultWebSessionManager sessionManager(){
    final DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
    return sessionManager;
  }
}
