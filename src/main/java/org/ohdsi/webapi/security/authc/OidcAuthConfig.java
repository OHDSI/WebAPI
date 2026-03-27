package org.ohdsi.webapi.security.authc;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.ohdsi.webapi.security.authz.AuthorizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;

@Configuration
@ConditionalOnProperty(prefix = "security.auth.openId", name = "enabled", havingValue = "true")
public class OidcAuthConfig {

  private static final Logger log = LoggerFactory.getLogger(OidcAuthConfig.class);
  private static final long STATE_TTL_SECONDS = 300; // 5 minutes

  private final HttpSecurityShared httpSecurityShared;
  private final AuthorizationService authorizationService;

  @Value("${security.auth.openId.clientId}")
  private String clientId;

  @Value("${security.auth.openId.apiSecret}")
  private String clientSecret;

  @Value("${security.auth.openId.url}")
  private String discoveryUrl;

  @Value("${security.auth.openId.externalUrl:}")
  private String externalUrl;

  @Value("${security.auth.openId.extraScopes:}")
  private String extraScopes;

  @Value("${security.auth.openId.rolesClaim:}")
  private String rolesClaim;

  @Value("${security.auth.oauth.callback.api}")
  private String callbackApi;

  @Value("${security.auth.oauth.callback.ui}")
  private String callbackUi;

  // Discovered endpoints
  private String authorizationEndpoint;
  private String tokenEndpoint;
  private String jwksUri;
  private String issuer;
  private JwtDecoder idTokenDecoder;

  // State store for CSRF protection
  private final ConcurrentHashMap<String, Instant> stateStore = new ConcurrentHashMap<>();

  public OidcAuthConfig(HttpSecurityShared httpSecurityShared, AuthorizationService authorizationService) {
    this.httpSecurityShared = httpSecurityShared;
    this.authorizationService = authorizationService;
  }

  @PostConstruct
  void discover() {
    log.info("OIDC: Fetching discovery document from {}", discoveryUrl);
    try {
      RestTemplate rest = new RestTemplate();
      Map<String, Object> doc = rest.exchange(
          discoveryUrl, HttpMethod.GET, null,
          new ParameterizedTypeReference<Map<String, Object>>() {}).getBody();

      if (doc == null) {
        throw new IllegalStateException("OIDC discovery document is empty");
      }

      authorizationEndpoint = (String) doc.get("authorization_endpoint");
      tokenEndpoint = (String) doc.get("token_endpoint");
      jwksUri = (String) doc.get("jwks_uri");
      issuer = (String) doc.get("issuer");

      if (authorizationEndpoint == null || tokenEndpoint == null || jwksUri == null) {
        throw new IllegalStateException(
            "OIDC discovery document missing required endpoints (authorization_endpoint, token_endpoint, jwks_uri)");
      }

      idTokenDecoder = NimbusJwtDecoder.withJwkSetUri(jwksUri).build();

      log.info("OIDC: Discovery complete. issuer={}, authz={}, token={}", issuer, authorizationEndpoint, tokenEndpoint);
    } catch (Exception e) {
      log.error("OIDC: Failed to fetch discovery document from {}. OIDC login will not work.", discoveryUrl, e);
    }
  }

  @Bean
  @Order(2)
  public SecurityFilterChain oidcAuthChain(HttpSecurity http) throws Exception {
    httpSecurityShared.configureDefaults(http);
    http.securityMatcher("/user/login/openid", "/user/oauth/callback")
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
    return http.build();
  }

  /**
   * Generate a state token and store it for CSRF validation.
   */
  public String createState() {
    evictExpiredStates();
    String state = UUID.randomUUID().toString();
    stateStore.put(state, Instant.now());
    return state;
  }

  /**
   * Validate and consume a state token. Returns true if valid.
   */
  public boolean validateState(String state) {
    Instant created = stateStore.remove(state);
    if (created == null) {
      return false;
    }
    return Instant.now().isBefore(created.plusSeconds(STATE_TTL_SECONDS));
  }

  /**
   * Build the authorization URL for redirecting the user to the IdP.
   */
  public String buildAuthorizationUrl(String state) {
    // Use externalUrl for the authorization endpoint if configured (browser-accessible URL)
    String authEndpoint = resolveExternalAuthEndpoint();

    StringBuilder url = new StringBuilder(authEndpoint);
    url.append("?response_type=code");
    url.append("&client_id=").append(encode(clientId));
    url.append("&redirect_uri=").append(encode(callbackApi));
    url.append("&state=").append(encode(state));

    String scope = "openid";
    if (extraScopes != null && !extraScopes.isBlank()) {
      scope = scope + " " + extraScopes.trim();
    }
    url.append("&scope=").append(encode(scope));

    return url.toString();
  }

  /**
   * Exchange an authorization code for tokens, decode the ID token, and return it.
   */
  public Jwt exchangeCodeForIdToken(String code) {
    RestTemplate rest = new RestTemplate();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "authorization_code");
    params.add("code", code);
    params.add("redirect_uri", callbackApi);
    params.add("client_id", clientId);
    params.add("client_secret", clientSecret);

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

    ResponseEntity<Map<String, Object>> response = rest.exchange(
        tokenEndpoint, HttpMethod.POST, request,
        new ParameterizedTypeReference<Map<String, Object>>() {});

    Map<String, Object> body = response.getBody();
    if (body == null || !body.containsKey("id_token")) {
      throw new IllegalStateException("OIDC token response missing id_token");
    }

    String idTokenStr = (String) body.get("id_token");
    return idTokenDecoder.decode(idTokenStr);
  }

  /**
   * Extract roles from the ID token using the configured rolesClaim.
   * Supports dot-notation for nested claims (e.g., "realm_access.roles").
   */
  @SuppressWarnings("unchecked")
  public List<String> extractRoles(Jwt idToken) {
    if (rolesClaim == null || rolesClaim.isBlank()) {
      return Collections.emptyList();
    }

    String[] parts = rolesClaim.split("\\.");
    Object current = idToken.getClaims();

    for (String part : parts) {
      if (current instanceof Map) {
        current = ((Map<String, Object>) current).get(part);
      } else {
        log.warn("OIDC: Cannot traverse claim path '{}' - intermediate value is not a map", rolesClaim);
        return Collections.emptyList();
      }
      if (current == null) {
        log.debug("OIDC: Claim '{}' not found in ID token", rolesClaim);
        return Collections.emptyList();
      }
    }

    if (current instanceof List<?> list) {
      List<String> roles = new ArrayList<>();
      for (Object item : list) {
        if (item instanceof String s) {
          roles.add(s);
        }
      }
      return roles;
    } else if (current instanceof String s) {
      return List.of(s);
    }

    log.warn("OIDC: Claim '{}' is not a list or string: {}", rolesClaim, current.getClass().getName());
    return Collections.emptyList();
  }

  /**
   * Full-sync roles from the IdP token to the user in the database.
   * Adds roles present in the token, removes OPENID-origin roles not in the token.
   * Only touches roles with OPENID origin.
   */
  public void syncRoles(String login, List<String> idpRoles) {
    List<String> currentOidcRoleNames = getOidcOriginRoleNames(login);

    // Add new roles from the token
    for (String roleName : idpRoles) {
      if (!currentOidcRoleNames.contains(roleName)) {
        try {
          authorizationService.addUserToRole(roleName, login, UserOrigin.OPENID);
          log.info("OIDC: Added role '{}' to user '{}'", roleName, login);
        } catch (Exception e) {
          log.warn("OIDC: Could not add role '{}' to user '{}': {}", roleName, login, e.getMessage());
        }
      }
    }

    // Remove OPENID-origin roles no longer in the token
    for (String roleName : currentOidcRoleNames) {
      if (!idpRoles.contains(roleName)) {
        try {
          authorizationService.removeUserFromRole(roleName, login, UserOrigin.OPENID);
          log.info("OIDC: Removed role '{}' from user '{}'", roleName, login);
        } catch (Exception e) {
          log.warn("OIDC: Could not remove role '{}' from user '{}': {}", roleName, login, e.getMessage());
        }
      }
    }
  }

  public String getCallbackUi() {
    return callbackUi;
  }

  // --- private helpers ---

  private List<String> getOidcOriginRoleNames(String login) {
    try {
      return authorizationService.getOidcOriginRoles(login);
    } catch (Exception e) {
      log.warn("OIDC: Could not fetch OPENID-origin roles for user {}: {}", login, e.getMessage());
      return Collections.emptyList();
    }
  }

  private String resolveExternalAuthEndpoint() {
    if (externalUrl != null && !externalUrl.isBlank() && authorizationEndpoint != null) {
      // Replace the host portion of the authorization endpoint with the external URL
      // e.g., internal: http://mock-oauth2:9090/default/authorize
      //        external: http://localhost:9090/default
      //        result:   http://localhost:9090/default/authorize
      try {
        URI authUri = URI.create(authorizationEndpoint);
        URI extUri = URI.create(externalUrl);
        // Take the path suffix from authorizationEndpoint that extends beyond the discovery path
        String discoveryBase = discoveryUrl.replace("/.well-known/openid-configuration", "");
        URI discoveryBaseUri = URI.create(discoveryBase);
        String pathSuffix = authUri.getPath().substring(discoveryBaseUri.getPath().length());
        return extUri.toString() + pathSuffix;
      } catch (Exception e) {
        log.warn("OIDC: Could not resolve external auth endpoint, using discovery value", e);
      }
    }
    return authorizationEndpoint;
  }

  private void evictExpiredStates() {
    Instant cutoff = Instant.now().minusSeconds(STATE_TTL_SECONDS);
    Iterator<Map.Entry<String, Instant>> it = stateStore.entrySet().iterator();
    while (it.hasNext()) {
      if (it.next().getValue().isBefore(cutoff)) {
        it.remove();
      }
    }
  }

  private static String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
}
