package org.ohdsi.webapi.security.authc;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * This configuration class is created as a bean, and Spring will inject it to handle the requests matching the 
 * value passed in registerCorsConfiguration(). This prevents the need to configure each SecurityFilterChain individually.
 */
@Configuration
public class CorsConfig {

	@Bean
	public CorsConfigurationSource corsConfigurationSource(@Value("${security.cors.allowed-origins}") String[] allowedOrigins) {
		CorsConfiguration config = new CorsConfiguration();

		config.setAllowedOrigins(Arrays.asList(allowedOrigins));
		config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(Arrays.asList("*"));
		config.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		// Apply CORS rules to all paths
		source.registerCorsConfiguration("/**", config);

		return source;
	}
}