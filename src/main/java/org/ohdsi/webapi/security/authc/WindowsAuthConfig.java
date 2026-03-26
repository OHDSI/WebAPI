package org.ohdsi.webapi.security.authc;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

import waffle.servlet.spi.NegotiateSecurityFilterProvider;
import waffle.servlet.spi.SecurityFilterProvider;
import waffle.servlet.spi.SecurityFilterProviderCollection;
import waffle.spring.NegotiateSecurityFilter;
import waffle.spring.NegotiateSecurityFilterEntryPoint;
import waffle.windows.auth.impl.WindowsAuthProviderImpl;

@Configuration
@ConditionalOnProperty(prefix = "security.auth.windows", name = "enabled", havingValue = "true")
public class WindowsAuthConfig {

	private final HttpSecurityShared httpSecurityShared;

	public WindowsAuthConfig(HttpSecurityShared httpSecurityShared) {
		this.httpSecurityShared = httpSecurityShared;
	}

	@Bean
	@Order(1)
	public SecurityFilterChain windowsAuthChain(HttpSecurity http) throws Exception {

    // Waffle filters wrap native providers iniside filter providers, and builds a collection.
    WindowsAuthProviderImpl windowsAuthProvider = new WindowsAuthProviderImpl();
    NegotiateSecurityFilterProvider filterProvider = new NegotiateSecurityFilterProvider(windowsAuthProvider);
    SecurityFilterProviderCollection providers = new SecurityFilterProviderCollection(new SecurityFilterProvider[]{filterProvider});

    // the entry ponit filter initiates negotation from a authentication exception, the negotiate filter performs the actual auth.
    NegotiateSecurityFilterEntryPoint entryFilter = new NegotiateSecurityFilterEntryPoint();
    entryFilter.setProvider(providers);
    NegotiateSecurityFilter negotiateFilter = new NegotiateSecurityFilter();
    negotiateFilter.setProvider(providers);

		httpSecurityShared.configureDefaults(http);

		http
				.securityMatcher("/user/login/windows")
				// ⬇️ REQUIRE authentication
				.authorizeHttpRequests(authz -> authz.anyRequest().authenticated())
				// ⬇️ This is what triggers the Negotiate challenge
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint(entryFilter))
				.addFilterBefore(negotiateFilter,  AuthorizationFilter.class);

		return http.build();
	}  

}