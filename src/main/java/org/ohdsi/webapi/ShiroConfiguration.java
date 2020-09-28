package org.ohdsi.webapi;

import java.util.Collection;
import java.util.Set;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.ohdsi.webapi.shiro.AtlasWebSecurityManager;
import org.ohdsi.webapi.shiro.lockout.*;
import org.ohdsi.webapi.shiro.management.DataSourceAccessBeanPostProcessor;
import org.ohdsi.webapi.shiro.management.DisabledSecurity;
import org.ohdsi.webapi.shiro.management.Security;
import org.ohdsi.webapi.shiro.management.datasource.DataSourceAccessParameterResolver;
import org.ohdsi.webapi.shiro.realms.JwtAuthRealm;
import org.ohdsi.webapi.shiro.subject.WebDelegatingRunAsSubjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by GMalikov on 20.08.2015.
 */

@Configuration
public class ShiroConfiguration {

    @Value("${security.maxLoginAttempts}")
    private int maxLoginAttempts;
    @Value("${security.duration.initial}")
    private long initialDuration;
    @Value("${security.duration.increment}")
    private long increment;
    @Value("${spring.aop.proxy-target-class:false}")
    private Boolean proxyTargetClass;
    @Autowired
    protected ApplicationEventPublisher eventPublisher;

    @Bean
    public ShiroFilterFactoryBean shiroFilter(Security security, LockoutPolicy lockoutPolicy) {

        ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();
        shiroFilter.setSecurityManager(securityManager(security, lockoutPolicy));

        Map<String, Filter> filters = security.getFilters().entrySet().stream()
                .collect(Collectors.toMap(f -> f.getKey().getTemplateName(), Map.Entry::getValue));
        shiroFilter.setFilters(filters);
        shiroFilter.setFilterChainDefinitionMap(security.getFilterChain());

        return shiroFilter;
    }

    @Bean
    public DefaultWebSecurityManager securityManager(Security security, LockoutPolicy lockoutPolicy) {

        Set<Realm> realmsForAuthentication = security.getRealms();
        Collection<Realm> realmsForAuthorization = getJwtAuthRealmForAuthorization(security);

        final DefaultWebSecurityManager securityManager = new AtlasWebSecurityManager(
                lockoutPolicy,
                security.getAuthenticator(),
                realmsForAuthentication,
                realmsForAuthorization
        );

        securityManager.setSubjectFactory(new WebDelegatingRunAsSubjectFactory());

        return securityManager;
    }


    @Bean
    @ConditionalOnExpression("#{!'${security.provider}'.equals('AtlasRegularSecurity')}")
    public LockoutPolicy noLockoutPolicy() {

        return new NoLockoutPolicy();
    }

    @Bean
    @ConditionalOnProperty(name = "security.provider", havingValue = "AtlasRegularSecurity")
    public LockoutPolicy lockoutPolicy() {

        return new DefaultLockoutPolicy(lockoutStrategy(), maxLoginAttempts, eventPublisher);
    }

    @Bean
    @ConditionalOnProperty(name = "security.provider", havingValue = "AtlasRegularSecurity")
    public LockoutStrategy lockoutStrategy() {

        return new ExponentLockoutStrategy(initialDuration, increment, maxLoginAttempts);
    }

    @Bean
    @ConditionalOnMissingBean(value = DisabledSecurity.class)
    public DataSourceAccessBeanPostProcessor dataSourceAccessBeanPostProcessor(DataSourceAccessParameterResolver parameterResolver) {

        return new DataSourceAccessBeanPostProcessor(parameterResolver, proxyTargetClass);
    }

    private Collection<Realm> getJwtAuthRealmForAuthorization(Security security) {

        return security.getRealms().stream()
                .filter(JwtAuthRealm.class::isInstance)
                .collect(Collectors.toList());
    }

}
