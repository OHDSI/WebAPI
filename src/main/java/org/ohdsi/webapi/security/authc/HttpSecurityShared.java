package org.ohdsi.webapi.security.authc;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.stereotype.Component;

/**
 * Shared HTTP security configuration applied to all SecurityFilterChain beans.
 * 
 * Converted from static utility to Spring bean to allow injection of
 * configuration properties from application.yaml.
 */
@Component
public class HttpSecurityShared {

  public void configureDefaults(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        // Disable all unnecessary filters
        .requestCache(AbstractHttpConfigurer::disable)
        .sessionManagement(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable);
  }
}
