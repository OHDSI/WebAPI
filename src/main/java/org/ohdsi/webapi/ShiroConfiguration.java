package org.ohdsi.webapi;

import java.util.*;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.ohdsi.webapi.shiro.management.AtlasSecurity;
import org.ohdsi.webapi.shiro.management.Security;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Created by GMalikov on 20.08.2015.
 */

@Configuration
public class ShiroConfiguration {

  @Bean
  public PermissionManager authorizer() {
    return new PermissionManager();
  }

  @Bean
  @Primary
  public Security security() {
    return new AtlasSecurity();
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
}
