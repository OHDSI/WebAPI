package org.ohdsi.webapi;

import java.util.Collection;
import java.util.Set;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.AbstractShiroFilter;
import org.ohdsi.webapi.shiro.AtlasWebSecurityManager;
import org.ohdsi.webapi.shiro.lockout.*;
import org.ohdsi.webapi.shiro.management.Security;
import org.ohdsi.webapi.shiro.realms.JwtAuthRealm;
import org.ohdsi.webapi.shiro.subject.WebDelegatingRunAsSubjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;

import jakarta.servlet.Filter;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by GMalikov on 20.08.2015.
 */

@Configuration
@Lazy(false)
public class ShiroConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ShiroConfiguration.class);

    @Value("${security.maxLoginAttempts}")
    private int maxLoginAttempts;
    @Value("${security.duration.initial}")
    private long initialDuration;
    @Value("${security.duration.increment}")
    private long increment;
    @Autowired
    protected ApplicationEventPublisher eventPublisher;

    @Bean(name = "shiroFilter")
    public ShiroFilterFactoryBean shiroFilter(Security security, LockoutPolicy lockoutPolicy) {

        ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();
        shiroFilter.setSecurityManager(securityManager(security, lockoutPolicy));

        Map<String, Filter> filters = security.getFilters().entrySet().stream()
                .collect(Collectors.toMap(f -> f.getKey().getTemplateName(), Map.Entry::getValue));
        shiroFilter.setFilters(filters);

        Map<String, String> filterChain = security.getFilterChain();

        // Debug: log the filter chain configuration
        log.info("=== Shiro Filter Chain Configuration ===");
        log.info("Security implementation: {}", security.getClass().getName());
        log.info("Number of filters: {}", filters.size());
        log.info("Filter names: {}", filters.keySet());
        log.info("Filter chain paths ({} entries):", filterChain.size());
        filterChain.forEach((path, chain) -> log.info("  {} -> {}", path, chain));
        log.info("=== End Shiro Filter Chain Configuration ===");

        shiroFilter.setFilterChainDefinitionMap(filterChain);

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

        // Initialize SecurityUtils for programmatic access throughout the application
        SecurityUtils.setSecurityManager(securityManager);

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

    /**
     * Register the Shiro filter with the servlet container.
     * This is necessary for Spring Boot to properly apply the filter to all requests.
     */
    @Bean
    public FilterRegistrationBean<AbstractShiroFilter> shiroFilterRegistration(ShiroFilterFactoryBean shiroFilterFactoryBean) throws Exception {
        FilterRegistrationBean<AbstractShiroFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter((AbstractShiroFilter) shiroFilterFactoryBean.getObject());
        registration.addUrlPatterns("/*");
        registration.setName("shiroFilter");
        registration.setOrder(1); // Run before other filters
        return registration;
    }

    private Collection<Realm> getJwtAuthRealmForAuthorization(Security security) {

        return security.getRealms().stream()
                .filter(JwtAuthRealm.class::isInstance)
                .collect(Collectors.toList());
    }

}
