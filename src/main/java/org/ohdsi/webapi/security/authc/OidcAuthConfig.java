package org.ohdsi.webapi.security.authc;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
  private static final String DISCOVERY_SUFFIX = "/.well-known/openid-configuration";

  private final HttpSecurityShared httpSecurityShared;
  private final LoginService loginService;
  private final OneTimeCodeService oneTimeCodeService;
  private final org.ohdsi.webapi.security.authz.mapping.OidcGroupToRoleMapper oidcGroupToRoleMapper;

  @Value("${security.auth.oidc.clientId}")
  private String clientId;

  @Value("${security.auth.oidc.apiSecret}")
  private String clientSecret;

  @Value("${security.auth.oidc.url}")
  private String discoveryOrIssuerUrl;

  @Value("${security.auth.oidc.externalUrl:}")
  private String externalUrl;

  // Base URL that serves the same provider as the issuer but is reachable from
  // inside the deployment. The issuer a provider advertises has to stay the one
  // its tokens carry, which in a split-horizon deployment is a public address the
  // server itself cannot resolve; discovery and JWKS are then fetched here while
  // the public issuer remains what tokens are validated against. Blank keeps the
  // plain behaviour of resolving everything from the issuer.
  @Value("${security.auth.oidc.internalUrl:}")
  private String internalUrl;

  @Value("${security.auth.oidc.extraScopes:}")
  private String extraScopes;

  @Value("${security.auth.oidc.rolesClaim:}")
  private String rolesClaim;

  @Value("${security.auth.oidc.rolesToUpperCase:true}")
  private boolean rolesToUpperCase;

  @Value("${security.auth.oauth.callback.ui}")
  private String callbackUi;

  @Value("${security.auth.oidc.enabled:false}")
  private boolean oidcRuntimeEnabled;

  public OidcAuthConfig(HttpSecurityShared httpSecurityShared,
                        LoginService loginService,
                        OneTimeCodeService oneTimeCodeService,
                        org.ohdsi.webapi.security.authz.mapping.ExternalRoleMapService externalRoleMapService) {
    this.httpSecurityShared = httpSecurityShared;
    this.loginService = loginService;
    this.oneTimeCodeService = oneTimeCodeService;
    this.oidcGroupToRoleMapper = new org.ohdsi.webapi.security.authz.mapping.OidcGroupToRoleMapper(externalRoleMapService);
  }

  @Bean
  public ClientRegistrationRepository oidcClientRegistrationRepository() {
    if (!oidcRuntimeEnabled || discoveryOrIssuerUrl == null || discoveryOrIssuerUrl.isBlank()) {
      log.info("OIDC: baked into the native image but disabled at runtime — "
          + "registering an empty client registration repository (no discovery fetch)");
      return registrationId -> null;
    }
    String issuer = stripDiscoverySuffix(discoveryOrIssuerUrl);
    String internalBase = normaliseBase(internalUrl);

    ClientRegistration.Builder builder;
    if (internalBase == null) {
      log.info("OIDC: Discovering provider metadata from issuer {}", issuer);
      builder = ClientRegistrations.fromIssuerLocation(issuer);
    } else {
      log.info("OIDC: Discovering provider metadata for issuer {} via internal base {}", issuer, internalBase);
      builder = ClientRegistrations.fromOidcConfiguration(
          internalConfiguration(issuer, internalBase));
    }
    builder
        .registrationId("openid")
        .clientId(clientId)
        .clientSecret(clientSecret)
        .redirectUri("{baseUrl}/user/oauth/callback/{registrationId}")
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
  public SecurityFilterChain oidcAuthChain(
      HttpSecurity http,
      ClientRegistrationRepository clientRegistrationRepository) throws Exception {
    httpSecurityShared.configureDefaults(http);
    http
        .securityMatcher("/user/login/openid", "/user/oauth/callback/openid")
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .oauth2Login(oauth -> oauth
            .clientRegistrationRepository(clientRegistrationRepository)
            .authorizationEndpoint(authz -> authz.baseUri("/user/login"))
            .redirectionEndpoint(redir -> redir.baseUri("/user/oauth/callback/openid"))
            .successHandler(this::handleSuccess)
            .failureHandler((req, res, ex) -> {
              log.warn("OIDC: Authentication failed: {}", ex.getMessage());
              res.sendRedirect(callbackUi + "?error=oidc_failed");
            }));
    return http.build();
  }

  private void handleSuccess(HttpServletRequest request,
                             HttpServletResponse response,
                             Authentication authentication) throws IOException {
    OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
    String login = oidcUser.getSubject().toLowerCase();
    String name = firstNonBlank(oidcUser.getFullName(), oidcUser.getEmail(), login);

    log.info("OIDC: Authenticated user sub={}", login);

    // Extract and map roles from token claims
    Set<String> roles = oidcGroupToRoleMapper.extractAndMapRoles(
        oidcUser.getClaims(),
        rolesClaim,
        rolesToUpperCase);

    AuthenticatedLogin authenticatedLogin = AuthenticatedLogin.builder()
        .login(login)
        .name(name)
        .origin(UserOrigin.OIDC)
        .roles(roles)
        .originAuthentication(authentication)
        .build();

    // ✅ Embed JWT in OTC instead of returning in fragment
    LoginService.Result result = loginService.onSuccess(authenticatedLogin);
    UUID otc = oneTimeCodeService.generateCode(
        login,
        UserOrigin.OIDC,
        result.jwt());

    // Invalidate the HTTP session to prevent OAuth2 context from persisting
    request.getSession().invalidate();
    response.sendRedirect(callbackUi + "?code=" + otc);
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

  // Fetches the discovery document from the internally reachable base and points
  // the endpoints the server itself calls back at that base. The issuer and the
  // endpoints a browser is sent to are deliberately left on their public
  // addresses: rewriting those would put an unreachable host in front of the user
  // and break issuer validation.
  private static Map<String, Object> internalConfiguration(String issuer, String internalBase) {
    Map<String, Object> configuration = fetchConfiguration(internalBase + DISCOVERY_SUFFIX);
    for (String endpoint : List.of("token_endpoint", "jwks_uri", "userinfo_endpoint")) {
      Object value = configuration.get(endpoint);
      if (value instanceof String url) {
        configuration.put(endpoint, toInternal(url, issuer, internalBase));
      }
    }
    return configuration;
  }

  private static Map<String, Object> fetchConfiguration(String url) {
    try {
      HttpResponse<String> response = HttpClient.newBuilder()
          // Ask for HTTP/1.1 outright. The default attempts an HTTP/2 upgrade,
          // which a provider that only speaks 1.1 answers with a 502 rather than
          // by declining the upgrade -- discovery then fails against a provider
          // that is serving the document perfectly well.
          .version(HttpClient.Version.HTTP_1_1)
          .connectTimeout(Duration.ofSeconds(10))
          .build()
          .send(HttpRequest.newBuilder(URI.create(url))
                  .timeout(Duration.ofSeconds(10))
                  .GET()
                  .build(),
              HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() != 200) {
        throw new IllegalStateException(
            "OIDC discovery at " + url + " returned HTTP " + response.statusCode());
      }
      return new ObjectMapper().readValue(response.body(), new TypeReference<Map<String, Object>>() {});
    } catch (IOException e) {
      throw new IllegalStateException("Could not read OIDC discovery from " + url, e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted reading OIDC discovery from " + url, e);
    }
  }

  private static String toInternal(String url, String issuer, String internalBase) {
    return url.startsWith(issuer) ? internalBase + url.substring(issuer.length()) : url;
  }

  private static String normaliseBase(String url) {
    if (url == null || url.isBlank()) {
      return null;
    }
    String base = url.trim();
    if (base.endsWith(DISCOVERY_SUFFIX)) {
      base = base.substring(0, base.length() - DISCOVERY_SUFFIX.length());
    }
    while (base.endsWith("/")) {
      base = base.substring(0, base.length() - 1);
    }
    return base;
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
  public class OpenidDirect {

    private static final Logger log = LoggerFactory.getLogger(OpenidDirect.class);

    private final LoginService loginService;
    private final OneTimeCodeService oneTimeCodeService;
    private final org.ohdsi.webapi.security.authz.mapping.OidcGroupToRoleMapper oidcGroupToRoleMapper;
    private final JwtDecoder jwtDecoder;
    private final String rolesClaim;
    private final boolean rolesToUpperCase;

    public OpenidDirect(
        LoginService loginService,
        OneTimeCodeService oneTimeCodeService,
        org.ohdsi.webapi.security.authz.mapping.ExternalRoleMapService externalRoleMapService,
        @Value("${security.auth.oidc.enabled:false}") boolean enabled,
        @Value("${security.auth.oidc.url}") String discoveryOrIssuerUrl,
        @Value("${security.auth.oidc.internalUrl:}") String internalUrl,
        @Value("${security.auth.oidc.rolesClaim:}") String rolesClaim,
        @Value("${security.auth.oidc.rolesToUpperCase:true}") boolean rolesToUpperCase) {
      this.loginService = loginService;
      this.oneTimeCodeService = oneTimeCodeService;
      this.oidcGroupToRoleMapper = new org.ohdsi.webapi.security.authz.mapping.OidcGroupToRoleMapper(externalRoleMapService);
      this.rolesClaim = rolesClaim;
      this.rolesToUpperCase = rolesToUpperCase;
      if (!enabled || discoveryOrIssuerUrl == null || discoveryOrIssuerUrl.isBlank()) {
        log.info("OIDC direct: disabled at runtime — /user/login/openidDirect inactive");
        this.jwtDecoder = null;
        return;
      }
      String issuer = stripDiscoverySuffixStatic(discoveryOrIssuerUrl);
      String internalBase = normaliseBase(internalUrl);
      // Accept both `JWT` and `at+jwt` (RFC 9068) header types — Logto and other
      // providers tag access tokens as `at+jwt`, which Spring's default verifier rejects.
      NimbusJwtDecoder decoder;
      if (internalBase == null) {
        log.info("OIDC direct: building JwtDecoder for issuer {}", issuer);
        decoder = NimbusJwtDecoder.withIssuerLocation(issuer)
            .jwtProcessorCustomizer(processor -> processor.setJWSTypeVerifier(
                new DefaultJOSEObjectTypeVerifier<>(
                    JOSEObjectType.JWT,
                    new JOSEObjectType("at+jwt"),
                    null)))
            .build();
      } else {
        // Same split-horizon concession as the registration repository: the keys
        // are fetched over the internal base, but tokens are still only accepted
        // when they carry the public issuer.
        Object jwks = internalConfiguration(issuer, internalBase).get("jwks_uri");
        if (!(jwks instanceof String jwkSetUri)) {
          throw new IllegalStateException("OIDC discovery at " + internalBase + " advertised no jwks_uri");
        }
        log.info("OIDC direct: building JwtDecoder for issuer {} with keys from {}", issuer, jwkSetUri);
        decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
            .jwtProcessorCustomizer(processor -> processor.setJWSTypeVerifier(
                new DefaultJOSEObjectTypeVerifier<>(
                    JOSEObjectType.JWT,
                    new JOSEObjectType("at+jwt"),
                    null)))
            .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
      }
      this.jwtDecoder = decoder;
    }

    @GetMapping("/user/login/openidDirect")
    public ResponseEntity<OneTimeCodeResponse> login(
        @RequestHeader(value = "Authorization", required = false) String auth) {

      if (jwtDecoder == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new OneTimeCodeResponse(null, null));
      }
      if (auth == null || !auth.regionMatches(true, 0, "Bearer ", 0, 7)) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new OneTimeCodeResponse(null, null));
      }
      String token = auth.substring(7).trim();

      Jwt jwt;
      try {
        jwt = jwtDecoder.decode(token);
      } catch (JwtException e) {
        log.warn("OIDC direct: token validation failed: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new OneTimeCodeResponse(null, null));
      }

      String login = jwt.getSubject().toLowerCase();
      String name = firstNonBlankStatic(
          jwt.getClaimAsString("name"),
          jwt.getClaimAsString("email"),
          login);

      log.info("OIDC direct: authenticated user sub={}", login);

      // Extract and map roles from token claims
      Set<String> roles = oidcGroupToRoleMapper.extractAndMapRoles(
          jwt.getClaims(),
          rolesClaim,
          rolesToUpperCase);

      AuthenticatedLogin authenticatedLogin = AuthenticatedLogin.builder()
          .login(login)
          .name(name)
          .origin(UserOrigin.OIDC)
          .roles(roles)
          .originAuthentication(null)  // Direct JWT flow doesn't have Spring Authentication
          .build();

      // ✅ Embed JWT in OTC instead of returning it directly
      LoginService.Result result = loginService.onSuccess(authenticatedLogin);
      UUID otc = oneTimeCodeService.generateCode(
          login,
          UserOrigin.OIDC,
          result.jwt());

      return ResponseEntity.ok(new OneTimeCodeResponse(otc, java.time.Duration.ofMinutes(10)));
    }

    /**
     * Response DTO for OTC redemption endpoint.
     * Returns the generated OTC and its expiration time.
     */
    record OneTimeCodeResponse(UUID code, java.time.Duration expiresIn) {}

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
