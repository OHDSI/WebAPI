package org.ohdsi.webapi.security.authc;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ohdsi.webapi.security.authz.AuthorizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@ConditionalOnProperty(prefix = "security.auth.oidc", name = "enabled", havingValue = "true")
public class OidcAuthConfig {

  private static final Logger log = LoggerFactory.getLogger(OidcAuthConfig.class);
  private static final String REGISTRATION_ID = "openid";
  private static final String DISCOVERY_SUFFIX = "/.well-known/openid-configuration";

  private final HttpSecurityShared httpSecurityShared;
  private final AuthorizationService authorizationService;
  private final LoginService loginService;

  @Value("${security.auth.oidc.clientId}")
  private String clientId;

  @Value("${security.auth.oidc.apiSecret}")
  private String clientSecret;

  @Value("${security.auth.oidc.url}")
  private String discoveryOrIssuerUrl;

  @Value("${security.auth.oidc.externalUrl:}")
  private String externalUrl;

  @Value("${security.auth.oidc.extraScopes:}")
  private String extraScopes;

  @Value("${security.auth.oidc.rolesClaim:}")
  private String rolesClaim;

  @Value("${security.auth.oidc.rolesToUpperCase:true}")
  private boolean rolesToUpperCase;

  @Value("${security.auth.oauth.callback.api}")
  private String callbackApi;

  @Value("${security.auth.oauth.callback.ui}")
  private String callbackUi;

  @Value("${security.defaultRoles:}")
  private List<String> defaultRoles;

  public OidcAuthConfig(HttpSecurityShared httpSecurityShared,
                        AuthorizationService authorizationService,
                        LoginService loginService) {
    this.httpSecurityShared = httpSecurityShared;
    this.authorizationService = authorizationService;
    this.loginService = loginService;
  }

  @Bean
  public ClientRegistrationRepository oidcClientRegistrationRepository() {
    String issuer = stripDiscoverySuffix(discoveryOrIssuerUrl);
    log.info("OIDC: Discovering provider metadata from issuer {}", issuer);

    ClientRegistration.Builder builder = ClientRegistrations.fromIssuerLocation(issuer)
        .registrationId(REGISTRATION_ID)
        .clientId(clientId)
        .clientSecret(clientSecret)
        .redirectUri(joinPath(callbackApi, REGISTRATION_ID))
        .scope(buildScopes());

    if (externalUrl != null && !externalUrl.isBlank()) {
      String discoveredAuthUri = builder.build().getProviderDetails().getAuthorizationUri();
      String rewritten = rewriteAuthorizationUri(discoveredAuthUri);
      builder.authorizationUri(rewritten);
      log.info("OIDC: Using external authorization URI {}", rewritten);
    }

    return new InMemoryClientRegistrationRepository(builder.build());
  }

  @Bean
  @Order(0)
  public SecurityFilterChain oidcDirectAuthChain(HttpSecurity http) throws Exception {
    httpSecurityShared.configureDefaults(http);
    http
        .securityMatcher("/user/login/openidDirect")
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
    return http.build();
  }

  @Bean
  @Order(1)
  public SecurityFilterChain oidcAuthChain(HttpSecurity http) throws Exception {
    httpSecurityShared.configureDefaults(http);
    http
        .securityMatcher("/user/login/" + REGISTRATION_ID, "/user/oauth/callback/**")
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .oauth2Login(oauth -> oauth
            .authorizationEndpoint(authz -> authz.baseUri("/user/login"))
            .redirectionEndpoint(redir -> redir.baseUri("/user/oauth/callback/*"))
            .successHandler(this::handleSuccess)
            .failureHandler((req, res, ex) -> {
              log.warn("OIDC: Authentication failed: {}", ex.getMessage());
              res.sendRedirect(appendQueryParam(callbackUi, "error", "oidc_failed"));
            }));
    return http.build();
  }

  private void handleSuccess(HttpServletRequest request,
                             HttpServletResponse response,
                             Authentication authentication) throws IOException {
    OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
    // Lowercase so the DB row login matches mintSession's JWT subject (which lowercases via getName()).
    String login = oidcUser.getSubject().toLowerCase();
    String name = firstNonBlank(oidcUser.getFullName(), oidcUser.getEmail(), login);

    log.info("OIDC: Authenticated user sub={}", login);

    List<String> filteredDefaults = defaultRoles.stream().filter(s -> !s.isBlank()).toList();
    authorizationService.ensureUserExists(login, name, UserOrigin.OIDC, filteredDefaults);

    List<String> rawRoles = extractRoles(oidcUser.getClaims(), rolesClaim, rolesToUpperCase);
    List<String> idpRoles = authorizationService.filterToExistingRoles(rawRoles);
    if (rawRoles.size() != idpRoles.size()) {
      log.debug("OIDC: dropped {} unknown roles from token for user {}",
          rawRoles.size() - idpRoles.size(), login);
    }
    if (!idpRoles.isEmpty()) {
      log.info("OIDC: Syncing roles from token for user {}: {}", login, idpRoles);
    }
    authorizationService.syncOidcRoles(login, idpRoles);

    List<SimpleGrantedAuthority> authorities = idpRoles.stream()
        .map(SimpleGrantedAuthority::new)
        .toList();
Authentication wrapped = new UsernamePasswordAuthenticationToken(login, null, authorities);
    // mintSession — not onSuccess — so onSuccess's ensureUserExists doesn't overwrite the display name.
    LoginService.Result result = loginService.mintSession(wrapped);

    response.sendRedirect(appendFragmentParam(callbackUi, "token", result.jwt()));
  }

  private Set<String> buildScopes() {
    Set<String> scopes = new LinkedHashSet<>();
    scopes.add("openid");
    scopes.add("profile");
    scopes.add("email");
    if (extraScopes != null && !extraScopes.isBlank()) {
      for (String s : extraScopes.trim().split("\\s+")) {
        if (!s.isBlank()) {
          scopes.add(s);
        }
      }
    }
    return scopes;
  }

  private static String joinPath(String base, String segment) {
    return (base.endsWith("/") ? base : base + "/") + segment;
  }

  private static String appendQueryParam(String url, String key, String value) {
    // Insert before any URL fragment so SPA hash-route callbacks keep the param queryable.
    int fragmentIdx = url.indexOf('#');
    String base = fragmentIdx >= 0 ? url.substring(0, fragmentIdx) : url;
    String fragment = fragmentIdx >= 0 ? url.substring(fragmentIdx) : "";
    String separator = base.contains("?") ? "&" : "?";
    return base + separator + key + "=" + value + fragment;
  }

  private static String appendFragmentParam(String url, String key, String value) {
    int fragmentIdx = url.indexOf('#');
    if (fragmentIdx < 0) {
      return url + "#" + key + "=" + value;
    }
    String base = url.substring(0, fragmentIdx);
    String fragment = url.substring(fragmentIdx + 1);
    String separator = fragment.isEmpty() ? "" : "&";
    return base + "#" + fragment + separator + key + "=" + value;
  }

  private String stripDiscoverySuffix(String url) {
    if (url == null || url.isBlank()) {
      throw new IllegalStateException("security.auth.oidc.url must be configured when OIDC is enabled");
    }
    if (url.endsWith(DISCOVERY_SUFFIX)) {
      return url.substring(0, url.length() - DISCOVERY_SUFFIX.length());
    }
    return url;
  }

  private String rewriteAuthorizationUri(String authorizationUri) {
    try {
      URI authUri = URI.create(authorizationUri);
      URI extUri = URI.create(externalUrl);
      String discoveryBase = stripDiscoverySuffix(discoveryOrIssuerUrl);
      URI discoveryBaseUri = URI.create(discoveryBase);
      String pathSuffix = authUri.getPath().substring(discoveryBaseUri.getPath().length());
      String extBase = extUri.toString();
      if (extBase.endsWith("/")) {
        extBase = extBase.substring(0, extBase.length() - 1);
      }
      return extBase + pathSuffix;
    } catch (Exception e) {
      log.warn("OIDC: Could not rewrite authorization URI with externalUrl '{}', using discovered value '{}'",
          externalUrl, authorizationUri, e);
      return authorizationUri;
    }
  }

  private static String firstNonBlank(String... values) {
    if (values == null) return null;
    for (String v : values) {
      if (v != null && !v.isBlank()) return v;
    }
    return null;
  }

  static List<String> extractRoles(Map<String, Object> claims, String claimPath, boolean toUpperCase) {
    if (claimPath == null || claimPath.isBlank() || claims == null) {
      return List.of();
    }
    String[] parts = claimPath.split("\\.");
    Object current = claims;
    for (String part : parts) {
      if (current instanceof Map<?, ?> map) {
        current = map.get(part);
      } else {
        log.warn("OIDC: Cannot traverse claim path '{}' - intermediate value is not a map", claimPath);
        return List.of();
      }
      if (current == null) {
        log.debug("OIDC: Claim '{}' not found in ID token", claimPath);
        return List.of();
      }
    }

    if (current instanceof List<?> list) {
      List<String> roles = new ArrayList<>();
      for (Object item : list) {
        if (item instanceof String s) {
          roles.add(toUpperCase ? s.toUpperCase() : s);
        }
      }
      return roles;
    }
    if (current instanceof String s) {
      return List.of(toUpperCase ? s.toUpperCase() : s);
    }
    log.warn("OIDC: Claim '{}' is not a list or string: {}", claimPath, current.getClass().getName());
    return List.of();
  }

  @RestController
  @ConditionalOnProperty(prefix = "security.auth.oidc", name = "enabled", havingValue = "true")
  public static class OpenidDirect {

    private static final Logger log = LoggerFactory.getLogger(OpenidDirect.class);

    private final AuthorizationService authorizationService;
    private final LoginService loginService;
    private final JwtDecoder jwtDecoder;
    private final List<String> defaultRoles;
    private final String rolesClaim;
    private final boolean rolesToUpperCase;

    public OpenidDirect(
        AuthorizationService authorizationService,
        LoginService loginService,
        @Value("${security.auth.oidc.url}") String discoveryOrIssuerUrl,
        @Value("${security.auth.oidc.rolesClaim:}") String rolesClaim,
        @Value("${security.auth.oidc.rolesToUpperCase:true}") boolean rolesToUpperCase,
        @Value("${security.defaultRoles:}") List<String> defaultRoles) {
      this.authorizationService = authorizationService;
      this.loginService = loginService;
      String issuer = stripDiscoverySuffixStatic(discoveryOrIssuerUrl);
      log.info("OIDC direct: building JwtDecoder for issuer {}", issuer);
      // Accept both `JWT` and `at+jwt` (RFC 9068) header types — Logto and other
      // providers tag access tokens as `at+jwt`, which Spring's default verifier rejects.
      this.jwtDecoder = NimbusJwtDecoder.withIssuerLocation(issuer)
          .jwtProcessorCustomizer(processor -> processor.setJWSTypeVerifier(
              new DefaultJOSEObjectTypeVerifier<>(
                  JOSEObjectType.JWT,
                  new JOSEObjectType("at+jwt"),
                  null)))
          .build();
      this.defaultRoles = defaultRoles.stream().filter(s -> !s.isBlank()).toList();
      this.rolesClaim = rolesClaim;
      this.rolesToUpperCase = rolesToUpperCase;
    }

    @GetMapping("/user/login/openidDirect")
    public ResponseEntity<LoginService.Result> login(
        @RequestHeader(value = "Authorization", required = false) String auth) {

      if (auth == null || !auth.regionMatches(true, 0, "Bearer ", 0, 7)) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new LoginService.Result(null, null, null, "Missing Bearer token"));
      }
      String token = auth.substring(7).trim();

      Jwt jwt;
      try {
        jwt = jwtDecoder.decode(token);
      } catch (JwtException e) {
        log.warn("OIDC direct: token validation failed: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new LoginService.Result(null, null, null, "Invalid token"));
      }

      String login = jwt.getSubject().toLowerCase();
      String name = firstNonBlankStatic(
          jwt.getClaimAsString("name"),
          jwt.getClaimAsString("email"),
          login);

      log.info("OIDC direct: authenticated user sub={}", login);

      authorizationService.ensureUserExists(login, name, UserOrigin.OIDC, defaultRoles);

      List<String> rawRoles = extractRoles(jwt.getClaims(), rolesClaim, rolesToUpperCase);
      List<String> idpRoles = authorizationService.filterToExistingRoles(rawRoles);
      authorizationService.syncOidcRoles(login, idpRoles);

      List<SimpleGrantedAuthority> authorities = idpRoles.stream()
          .map(SimpleGrantedAuthority::new)
          .toList();
      Authentication wrapped = new UsernamePasswordAuthenticationToken(login, null, authorities);
      LoginService.Result result = loginService.mintSession(wrapped);

      return ResponseEntity.ok()
          .header("Bearer", result.jwt())
          .body(result);
    }

    private static String stripDiscoverySuffixStatic(String url) {
      if (url == null || url.isBlank()) {
        throw new IllegalStateException("security.auth.oidc.url must be configured when OIDC is enabled");
      }
      String suffix = "/.well-known/openid-configuration";
      return url.endsWith(suffix) ? url.substring(0, url.length() - suffix.length()) : url;
    }

    private static String firstNonBlankStatic(String... values) {
      if (values == null) return null;
      for (String v : values) {
        if (v != null && !v.isBlank()) return v;
      }
      return null;
    }
  }
}
