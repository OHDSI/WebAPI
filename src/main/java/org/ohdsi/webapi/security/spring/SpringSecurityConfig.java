package org.ohdsi.webapi.security.spring;

import org.ohdsi.webapi.security.authz.AuthorizationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@ImportRuntimeHints(WebApiSecurityRuntimeHints.class)
public class SpringSecurityConfig {

  @Bean
  public MethodSecurityExpressionHandler methodSecurityExpressionHandler(
      AuthorizationService authorizationService) {
    return new WebApiMethodSecurityExpressionHandler(authorizationService);
  }
}
