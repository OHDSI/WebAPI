package org.ohdsi.webapi;

import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.ohdsi.webapi.shiro.management.AtlasSecurity;
import org.ohdsi.webapi.shiro.management.DisabledSecurity;
import org.ohdsi.webapi.shiro.management.Security;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * Created by GMalikov on 20.08.2015.
 */

@Configuration
public class ShiroConfiguration {
  private static final Log log = LogFactory.getLog(ShiroConfiguration.class);

  @Value("${security.enabled}")
  private boolean enabled;

  @Bean
  @DependsOn("flyway")
  public Security security() {
    if (enabled) {
      log.debug("AtlasSecurity module loaded");
      return new AtlasSecurity();
    }
    else {
      log.debug("DisabledSecurity module loaded");
      return new DisabledSecurity();
    }
  };

  @Bean
  public ShiroFilterFactoryBean shiroFilter(Security security){
    ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();
    shiroFilter.setSecurityManager(securityManager(security));

    shiroFilter.setFilters(security.getFilters());
    shiroFilter.setFilterChainDefinitionMap(security.getFilterChain());

    return shiroFilter;
  }

  @Bean
  public DefaultWebSecurityManager securityManager(Security security){
    final DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();

    securityManager.setAuthenticator(security.getAuthenticator());
    
    Set<Realm> realms = security.getRealms();
    if (realms != null && !realms.isEmpty())
      securityManager.setRealms(realms);

    return securityManager;
  }
}
