package org.ohdsi.webapi.security.authc;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

public class HttpSecurityShared {

  public static void configureDefaults(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        // Disable all unecessary filters
        .requestCache(AbstractHttpConfigurer::disable)
        .sessionManagement(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable)
        .anonymous(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable);
  }
}
