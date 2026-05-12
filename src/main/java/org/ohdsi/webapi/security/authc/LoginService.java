package org.ohdsi.webapi.security.authc;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.ohdsi.webapi.security.authz.AuthorizationService;
import org.ohdsi.webapi.security.authz.User;
import org.ohdsi.webapi.security.session.SessionProperties;
import org.ohdsi.webapi.security.session.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

  public record Result(
      String login,
      String jwt,
      String[] roles,
      String message) {
  }

  private final SessionService sessionService;
  private final JwtService jwtService;
  private final SessionProperties sessionProps;
  private final AuthorizationService authorizationService;
  private final List<String> defaultRoles;

  private static final Logger log = LoggerFactory.getLogger(LoginService.class);
  public final static Result NO_SESSION = new Result(null, null, null, "No session.");

  public LoginService(
      SessionService sessionService,
      AuthorizationService authorizationService,
      JwtService jwtService,
      @Value("${security.defaultRoles}") List<String> defaultRoles,
      SessionProperties sessionProps) {
    this.sessionService = sessionService;
    this.authorizationService = authorizationService;
    this.jwtService = jwtService;
    this.sessionProps = sessionProps;
    this.defaultRoles = defaultRoles.stream().filter(s -> !s.isBlank()).toList();
  }

  public Result onSuccess(Authentication authentication) {
    String login = authentication.getName().toLowerCase();
    authorizationService.ensureUserExists(login, login, null, this.defaultRoles);
    return mintSession(authentication);
  }

  // Skips ensureUserExists; for callers (e.g. OIDC) that provision the user themselves.
  public Result mintSession(Authentication authentication) {
    String login = authentication.getName().toLowerCase();
    log.info("LoginService: mintSession: " + login);

    String[] roles = authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .toArray(String[]::new);

    UUID sessionId = sessionService.createSession(login);
    Instant expiresAt = Instant.now().plus(sessionProps.getExpiration());
    String jwt = jwtService.generateToken(login, sessionId.toString(), Date.from(expiresAt));

    return new Result(login, jwt, roles, "Login successful");
  }

  /**
   * Impersonate another user. Creates a new session for the target user
   * and mints a JWT as that user.
   *
   * @param targetLogin the login of the user to impersonate
   * @return Result with JWT for the target user
   * @throws IllegalArgumentException if the target user does not exist
   */
  public Result runAs(String targetLogin) {
    final String login = targetLogin.toLowerCase();

    User targetUser = authorizationService.getUserByLogin(login)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + login));

    UUID sessionId = sessionService.createSession(login);
    Instant expiresAt = Instant.now().plus(sessionProps.getExpiration());
    String jwt = jwtService.generateToken(login, sessionId.toString(), Date.from(expiresAt));

    log.info("LoginService: runAs: impersonating {}", login);

    return new Result(login, jwt, new String[]{}, "Run-as successful");
  }

  /**
   * extends the authenticated session by minting a new JWT, and extending the
   * session in the session store.
   * 
   * @param authentication
   * @return
   */
  public Result extend(Authentication authentication) {

    // all non-authN requests should have a JWT token as the authenticated identity,
    // so this may be redundant:
    if (!(authentication instanceof WebApiAuthenticationToken webapiAuth)) {
      throw new BadCredentialsException("Invalid authentication type");
    }

    String login = webapiAuth.getName();
    UUID sessionId = webapiAuth.getSessionId();
    Instant expiresAt = Instant.now().plus(sessionProps.getExpiration());
    String[] roles = authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .toArray(String[]::new);

    // extend session
    sessionService.extendSession(sessionId, expiresAt);

    // mint the JWT
    String jwt = jwtService.generateToken(login, sessionId.toString(), Date.from(expiresAt));

    return new Result(login, jwt, roles, "Refreshed Token in for session");
  }

  /**
   * Revoke the session represented by the provided JWT authentication.
   */
  public Result logout(Authentication authentication) {
    if (!(authentication instanceof WebApiAuthenticationToken webapiAuth)) {
      String type = (authentication == null) ? "null" : authentication.getClass().getName();
      throw new BadCredentialsException("Invalid authentication type: " + type);
    }

    UUID sessionId = webapiAuth.getSessionId();

    try {
      sessionService.revokeSession(sessionId);
    } catch (IllegalArgumentException e) {
      throw new BadCredentialsException("Invalid session id", e);
    }

    String login = webapiAuth.getPrincipal().getName();

    return new Result(login, null, null, "Logout successful");
  }

  // Since login service initiates sessions, it can determine the cleanup schedule
  @Scheduled(fixedRateString = "#{@sessionProperties.cleanupInterval.toMillis()}")
  public void cleanupSessions() {
    sessionService.cleanupExpiredSessions();
  }

}
