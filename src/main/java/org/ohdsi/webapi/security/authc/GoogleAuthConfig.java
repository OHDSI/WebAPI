/*
 * Copyright 2024 Observational Health Data Sciences and Informatics [OHDSI.org].
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ohdsi.webapi.security.authc;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Spring Security configuration for Google OAuth2 authentication using OIDC.
 * 
 * Uses OIDC discovery to automatically obtain provider endpoints (authorization_endpoint,
 * token_endpoint, userinfo_endpoint, jwks_uri) from Google's metadata.
 * 
 * OAuth2 callback URI is automatically resolved by Spring Security's template mechanism:
 * {baseUrl} is extracted from the incoming request (respecting X-Forwarded-* headers),
 * and {registrationId} is replaced with "google", resulting in:
 * https://host:port/WebAPI/user/oauth/callback/google
 * 
 * Authentication flow:
 * 1. User clicks "Login with Google" → redirected to /user/login/google
 * 2. OAuth2Login filter redirects to Google's authorization endpoint
 * 3. User authenticates with Google, grants permissions
 * 4. Google redirects back to /user/oauth/callback/google with authorization code
 * 5. OAuth2Login exchanges code for ID token (OIDC)
 * 6. ID token signature validated using Google's JWKS
 * 7. handleGoogleSuccess() extracts user claims, creates session + mints JWT
 * 8. JWT embedded in OTC, OTC returned to client in query parameter
 * 9. Client calls /user/login/otc to redeem OTC for JWT
 */
@Configuration
@ConditionalOnProperty(prefix = "security.auth.oauth.google", name = "enabled", havingValue = "true")
public class GoogleAuthConfig {

  private static final Logger log = LoggerFactory.getLogger(GoogleAuthConfig.class);

  private final HttpSecurityShared httpSecurityShared;
  private final LoginService loginService;
  private final OneTimeCodeService oneTimeCodeService;

  @Value("${security.auth.oauth.google.apiKey}")
  private String clientId;

  @Value("${security.auth.oauth.google.apiSecret}")
  private String clientSecret;

  @Value("${security.auth.oauth.callback.ui}")
  private String callbackUi;

  @Value("${security.auth.oauth.google.enabled:false}")
  private boolean googleAuthEnabled;

  public GoogleAuthConfig(
      HttpSecurityShared httpSecurityShared,
      LoginService loginService,
      OneTimeCodeService oneTimeCodeService) {
    this.httpSecurityShared = httpSecurityShared;
    this.loginService = loginService;
    this.oneTimeCodeService = oneTimeCodeService;
  }

  /**
   * Register Google as an OAuth2 client using OIDC discovery.
   * Automatically discovers authorization, token, userinfo, and JWKS endpoints from
   * Google's /.well-known/openid-configuration metadata.
   * 
   * The redirect URI uses Spring Security's template mechanism: {baseUrl} is resolved
   * from the incoming request (respecting X-Forwarded-* headers and context path),
   * and {registrationId} is automatically replaced.
   */
  @Bean
  public ClientRegistrationRepository googleClientRegistrationRepository() {
    if (!googleAuthEnabled || clientId == null || clientSecret == null) {
      log.info("Google OAuth: disabled or misconfigured");
      return registrationId -> null;
    }

    log.info("Google OAuth: Configuring with OIDC discovery");

    // ✅ Use Spring Security's template-based redirect URI
    // {baseUrl} → resolved from request (respects X-Forwarded-* headers and context path)
    // {registrationId} → automatically replaced with "google"
    ClientRegistration registration = ClientRegistrations
        .fromIssuerLocation("https://accounts.google.com")
        .registrationId("google")
        .clientId(clientId)
        .clientSecret(clientSecret)
        .redirectUri("{baseUrl}/user/oauth/callback/{registrationId}")
        .scope("openid", "profile", "email")
        .build();

    return new InMemoryClientRegistrationRepository(registration);
  }

  /**
   * SecurityFilterChain for Google OAuth2 authentication.
   * Processes requests to /user/login/google and /user/oauth/callback/google.
   */
  @Bean
  @Order(2)  // After OIDC chains (Order 0, 1)
  public SecurityFilterChain googleAuthChain(
      HttpSecurity http,
      ClientRegistrationRepository clientRegistrationRepository) throws Exception {
    httpSecurityShared.configureDefaults(http);
    http
        .securityMatcher("/user/login/google", "/user/oauth/callback/google")
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .oauth2Login(oauth -> oauth
            .clientRegistrationRepository(clientRegistrationRepository)
            // ✅ Use OIDC user service — validates ID token signature via JWKS
            .userInfoEndpoint(userInfo -> userInfo
                .oidcUserService(new OidcUserService())
            )
            .authorizationEndpoint(authz -> authz.baseUri("/user/login"))
            .redirectionEndpoint(redir -> redir.baseUri("/user/oauth/callback/google"))
            .successHandler(this::handleGoogleSuccess)
            .failureHandler((req, res, ex) -> {
              log.warn("Google: Authentication failed: {}", ex.getMessage());
              res.sendRedirect(callbackUi + "?error=google_failed");
            }));
    return http.build();
  }

  /**
   * Handle successful Google authentication.
   * Extracts user claims, maps to WebAPI roles, creates session, mints JWT,
   * embeds JWT in OTC, and redirects with OTC in query parameter.
   */
  private void handleGoogleSuccess(HttpServletRequest request,
                                   HttpServletResponse response,
                                   Authentication authentication) throws IOException {
    OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
    String login = firstNonBlank(oidcUser.getEmail(), oidcUser.getSubject().toLowerCase());
    String name = firstNonBlank(oidcUser.getFullName(), oidcUser.getEmail(), login);

    log.info("Google: Authenticated user sub={}", login);

    // Google OIDC doesn't provide custom claims (roles/groups), therefore we do not perform mapping.
    AuthenticatedLogin authenticatedLogin = AuthenticatedLogin.builder()
        .login(login)
        .name(name)
        .origin(UserOrigin.GOOGLE)
        .originAuthentication(authentication)
        .build();

    // ✅ Key difference from old JWT flow:
    // loginService.onSuccess() returns JWT + creates session
    // We EMBED it in OTC instead of returning it in the fragment
    LoginService.Result result = loginService.onSuccess(authenticatedLogin);
    
    UUID otc = oneTimeCodeService.generateCode(
        login,
        UserOrigin.GOOGLE,
        result.jwt());

    // Return OTC to client in query param (not JWT in fragment)
    // Invalidate the HTTP session to prevent OAuth2 context from persisting
    request.getSession().invalidate();
    response.sendRedirect(callbackUi + "?code=" + otc);
  }

  private static String firstNonBlank(String... values) {
    if (values == null) return null;
    for (String v : values) {
      if (v != null && !v.isBlank()) return v;
    }
    return null;
  }
}
