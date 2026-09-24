package org.ohdsi.webapi.security.authc;

import java.util.List;

import javax.sql.DataSource;

import org.ohdsi.webapi.security.authc.db.AuthDataSourceProperties;
import org.ohdsi.webapi.security.authc.db.DatabaseAuthenticationProvider;
import org.ohdsi.webapi.security.authc.db.DatabaseUserDetailsService;
import org.ohdsi.webapi.security.authc.db.LockoutPolicyProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@ConditionalOnProperty(prefix = "security.auth.db", name = "enabled", havingValue = "true")
public class DatabaseAuthConfig {

  private final HttpSecurityShared httpSecurityShared;

  public DatabaseAuthConfig(HttpSecurityShared httpSecurityShared) {
    this.httpSecurityShared = httpSecurityShared;
  }

  @Bean
  DatabaseUserDetailsService dbUserDetailsService(
      @Qualifier("authDataSource") DataSource dataSource,
      AuthDataSourceProperties authDataSourceProperties) {
    return new DatabaseUserDetailsService(dataSource, authDataSourceProperties.getSchema());
  }

  @Bean(name = "authEncoder")
  public PasswordEncoder authEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  @Bean
  @ConfigurationProperties(prefix = "security.auth.db.lockout-policy")
  public LockoutPolicyProperties authLockoutProps() {
    return new LockoutPolicyProperties();
  }

  @Bean(name = "dbAuthenticationManager")
  public AuthenticationManager dbAuthenticationManager(
      DatabaseUserDetailsService dbUserDetailsService,
      LockoutPolicyProperties lockoutProps,
      PasswordEncoder authEncoder) {
    DatabaseAuthenticationProvider provider = new DatabaseAuthenticationProvider(dbUserDetailsService, authEncoder, lockoutProps);
    return new ProviderManager(List.of(provider));
  }

  @Bean
  @Order(1)
  public SecurityFilterChain databaseAuthChain(HttpSecurity http,
      @Qualifier("dbAuthenticationManager") AuthenticationManager authManager) throws Exception {

    httpSecurityShared.configureDefaults(http);

    http
      // Only apply this chain to DB login endpoints
      .securityMatcher("/user/login/db")
      // Let Spring handle Basic auth for GET requests
      .httpBasic(Customizer.withDefaults())
      // POST is handled by the controller (form params), GET requires Basic auth
      .authorizeHttpRequests(auth -> auth
        .requestMatchers(HttpMethod.POST, "/user/login/db").permitAll()
        .anyRequest().authenticated())
      .authenticationManager(authManager);

    return http.build();
  }
}