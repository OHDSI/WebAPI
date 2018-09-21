package org.ohdsi.webapi;

import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.ohdsi.webapi.cohortcharacterization.repository.CcGenerationEntityRepository;
import org.ohdsi.webapi.shiro.lockout.DefaultLockoutPolicy;
import org.ohdsi.webapi.shiro.lockout.ExponentLockoutStrategy;
import org.ohdsi.webapi.shiro.lockout.LockoutPolicy;
import org.ohdsi.webapi.shiro.lockout.LockoutStrategy;
import org.ohdsi.webapi.shiro.lockout.LockoutWebSecurityManager;
import org.ohdsi.webapi.shiro.lockout.NoLockoutPolicy;
import org.ohdsi.webapi.shiro.management.DataSourceAccessBeanPostProcessor;
import org.ohdsi.webapi.shiro.management.DisabledSecurity;
import org.ohdsi.webapi.shiro.management.Security;
import org.springframework.beans.factory.annotation.Autowired;
import org.ohdsi.webapi.shiro.management.datasource.DataSourceAccessParameterResolver;
import org.ohdsi.webapi.source.SourceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by GMalikov on 20.08.2015.
 */

@Configuration
public class ShiroConfiguration {
  private static final Log log = LogFactory.getLog(ShiroConfiguration.class);

  @Value("${security.maxLoginAttempts}")
  private int maxLoginAttempts;
  @Value("${security.duration.initial}")
  private long initialDuration;
  @Value("${security.duration.increment}")
  private long increment;
  @Autowired
  protected ApplicationEventPublisher eventPublisher;

  @Bean
  public ShiroFilterFactoryBean shiroFilter(Security security, LockoutPolicy lockoutPolicy){
    ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();
    shiroFilter.setSecurityManager(securityManager(security, lockoutPolicy));

    shiroFilter.setFilters(security.getFilters());
    shiroFilter.setFilterChainDefinitionMap(security.getFilterChain());

    return shiroFilter;
  }

  @Bean
  public DefaultWebSecurityManager securityManager(Security security, LockoutPolicy lockoutPolicy){
    final DefaultWebSecurityManager securityManager = new LockoutWebSecurityManager(lockoutPolicy, eventPublisher);

    securityManager.setAuthenticator(security.getAuthenticator());

    Set<Realm> realms = security.getRealms();
    if (realms != null && !realms.isEmpty())
      securityManager.setRealms(realms);

    return securityManager;
  }

  @Bean
  @ConditionalOnExpression("#{!'${security.provider}'.equals('AtlasRegularSecurity')}")
  public LockoutPolicy noLockoutPolicy(){

    return new NoLockoutPolicy();
  }

  @Bean
  @ConditionalOnProperty(name = "security.provider", havingValue = "AtlasRegularSecurity")
  public LockoutPolicy lockoutPolicy(){

    return new DefaultLockoutPolicy(lockoutStrategy(), maxLoginAttempts);
  }

  @Bean
  @ConditionalOnProperty(name = "security.provider", havingValue = "AtlasRegularSecurity")
  public LockoutStrategy lockoutStrategy(){

    return new ExponentLockoutStrategy(initialDuration, increment, maxLoginAttempts);
  }

  @Bean
  @ConditionalOnMissingBean(value = DisabledSecurity.class)
  public DataSourceAccessBeanPostProcessor dataSourceAccessBeanPostProcessor(DataSourceAccessParameterResolver parameterResolver) {

    return new DataSourceAccessBeanPostProcessor(parameterResolver);
  }

}
