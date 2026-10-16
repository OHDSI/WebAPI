package org.ohdsi.webapi.security;

import org.ohdsi.webapi.security.session.SessionProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This class creates/configures Beans that are used across the security domain
 * of the application. This is primarly to load configuration properties from
 * application.yaml for injection into services.
 */
@Configuration
public class SecurityConfig {
  @Bean
  @ConfigurationProperties(prefix = "security.sessions")
  public SessionProperties sessionProperties() {
    return new SessionProperties();
  }
}
