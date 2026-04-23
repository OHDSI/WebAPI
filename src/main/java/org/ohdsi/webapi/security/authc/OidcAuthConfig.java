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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;

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

  // Default true to mirror LdapAuthConfig's SimpleGrantedAuthority upper-casing, so roles from
  // the two IdPs collide in sec_role by name rather than creating parallel mixed-case duplicates.
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
    // Lowercase to match LoginService.mintSession which normalizes via Authentication.getName().toLowerCase();
    // a mixed-case sub would otherwise create the DB row as-is while the JWT encodes the lowercased form.
    String login = oidcUser.getSubject().toLowerCase();
    String name = firstNonBlank(oidcUser.getFullName(), oidcUser.getEmail(), login);

    log.info("OIDC: Authenticated user sub={}", login);

    List<String> filteredDefaults = defaultRoles.stream().filter(s -> !s.isBlank()).toList();
    authorizationService.ensureUserExists(login, name, UserOrigin.OIDC, filteredDefaults);

    List<String> idpRoles = extractRoles(oidcUser.getClaims(), rolesClaim, rolesToUpperCase);
    if (!idpRoles.isEmpty()) {
      log.info("OIDC: Syncing roles from token for user {}: {}", login, idpRoles);
      syncRoles(login, idpRoles);
    }

    List<SimpleGrantedAuthority> authorities = idpRoles.stream()
        .map(SimpleGrantedAuthority::new)
        .toList();
Authentication wrapped = new UsernamePasswordAuthenticationToken(login, null, authorities);
    // mintSession — not onSuccess — so onSuccess's ensureUserExists doesn't overwrite the display name.
    LoginService.Result result = loginService.mintSession(wrapped);

    response.sendRedirect(appendQueryParam(callbackUi, "token", result.jwt()));
  }

  private void syncRoles(String login, List<String> idpRoles) {
    List<String> currentOidcRoleNames;
    try {
      currentOidcRoleNames = authorizationService.getOidcOriginRoles(login);
    } catch (Exception e) {
      log.warn("OIDC: Could not fetch OIDC-origin roles for user {}: {}", login, e.getMessage());
      return;
    }

    for (String roleName : idpRoles) {
      if (!currentOidcRoleNames.contains(roleName)) {
        try {
          authorizationService.addUserToRole(roleName, login, UserOrigin.OIDC);
          log.info("OIDC: Added role '{}' to user '{}'", roleName, login);
        } catch (Exception e) {
          log.warn("OIDC: Could not add role '{}' to user '{}': {}", roleName, login, e.getMessage());
        }
      }
    }

    for (String roleName : currentOidcRoleNames) {
      if (!idpRoles.contains(roleName)) {
        try {
          authorizationService.removeUserFromRole(roleName, login, UserOrigin.OIDC);
          log.info("OIDC: Removed role '{}' from user '{}'", roleName, login);
        } catch (Exception e) {
          log.warn("OIDC: Could not remove role '{}' from user '{}': {}", roleName, login, e.getMessage());
        }
      }
    }
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
    String separator = url.contains("?") ? "&" : "?";
    return url + separator + key + "=" + value;
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
}
