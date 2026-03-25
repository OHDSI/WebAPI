package org.ohdsi.webapi.security.authc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@ConditionalOnProperty(prefix = "security.auth.ldap", name = "enabled", havingValue = "true")
public class LdapAuthConfig {

  private static final Logger log = LoggerFactory.getLogger(LdapAuthConfig.class);

  private final HttpSecurityShared httpSecurityShared;

  public LdapAuthConfig(HttpSecurityShared httpSecurityShared) {
    this.httpSecurityShared = httpSecurityShared;
  }

  @Value("${security.auth.ldap.url}")
  private String ldapUrl;

  @Value("${security.auth.ldap.base-dn}")
  private String baseDn;

  @Value("${security.auth.ldap.bind-dn}")
  private String bindDn; // specify an account in environments that don't allow anonymous searches

  @Value("${security.auth.ldap.bind-password}")
  private String bindPassword; // password for the binding account

  @Value("${security.auth.ldap.user-search-base:}")
  private String userSearchBase;

  @Value("${security.auth.ldap.user-filter}")
  private String userFilter;

  @Value("${security.auth.ldap.group-search-base}")
  private String groupSearchBase;

  @Value("${security.auth.ldap.group-filter}")
  private String groupFilter;

  @Value("${security.auth.ldap.group-role-attr}")
  private String groupRoleAttr;

  @Bean
  @Order(1)
  SecurityFilterChain ldapSecurityFilterChain(HttpSecurity http,
			CorsConfigurationSource corsConfigurationSource) throws Exception {

    // --- Context ---
    DefaultSpringSecurityContextSource contextSource = new DefaultSpringSecurityContextSource(ldapUrl + "/" + baseDn);

    // Optional service bind
    if (bindDn != null && !bindDn.isBlank()) {

      contextSource.setUserDn(bindDn);

      if (bindPassword == null || bindPassword.isBlank()) {
        throw new IllegalStateException("security.ldap.bind-dn is set but bind-password is missing or blank");
      }

      contextSource.setPassword(bindPassword);

      log.info("LDAP search will bind as service account [{}]", bindDn);

    } else {
      log.info("LDAP search will use anonymous bind");
    }

    contextSource.afterPropertiesSet();

    // --- Bind ---
    BindAuthenticator bindAuthenticator = new BindAuthenticator(contextSource);
    String effectiveUserSearchBase = (userSearchBase == null || userSearchBase.isBlank())
        ? ""
        : userSearchBase;

    bindAuthenticator.setUserSearch(
        new FilterBasedLdapUserSearch(
            effectiveUserSearchBase,
            userFilter,
            contextSource));

    // --- Group → Authority mapping ---
    DefaultLdapAuthoritiesPopulator authoritiesPopulator = new DefaultLdapAuthoritiesPopulator(contextSource,
        groupSearchBase);

    authoritiesPopulator.setGroupSearchFilter(groupFilter);
    authoritiesPopulator.setGroupRoleAttribute(groupRoleAttr);
    authoritiesPopulator.setIgnorePartialResultException(true);

    // --- Provider ---
    LdapAuthenticationProvider provider = new LdapAuthenticationProvider(bindAuthenticator, authoritiesPopulator);

    provider.setAuthoritiesMapper(authorities -> authorities.stream()
        .map(a -> new SimpleGrantedAuthority(a.getAuthority().toUpperCase()))
        .toList());

    ProviderManager authManager = new ProviderManager(provider);
    authManager.setAuthenticationEventPublisher(new DefaultAuthenticationEventPublisher());
    httpSecurityShared.configureDefaults(http);
    http
      .securityMatcher("/user/login/ldap")
      // Let Spring handle Basic auth
      .httpBasic(Customizer.withDefaults())
      // Attach the AuthenticationManager
      .authorizeHttpRequests(auth -> auth
        .anyRequest().authenticated())
      .authenticationManager(authManager);

    return http.build();
  }
}