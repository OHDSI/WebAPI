package org.ohdsi.webapi.security.spring;

import org.ohdsi.webapi.security.authz.AuthorizationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfig {

  /**
   * Method security is only enabled when security.provider is AtlasRegularSecurity.
   * When DisabledSecurity, @PreAuthorize annotations are not enforced.
   */
  @Configuration
  @ConditionalOnProperty(name = "security.provider", havingValue = "AtlasRegularSecurity")
  @EnableMethodSecurity
  public static class MethodSecurityConfig {

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(
        AuthorizationService authorizationService) {
      return new WebApiMethodSecurityExpressionHandler(authorizationService);
    }
  }
}
