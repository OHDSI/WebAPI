package org.ohdsi.webapi.security.authc;

import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.ohdsi.webapi.security.authz.AuthorizationService;
import org.ohdsi.webapi.security.authz.Role;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

  /**
   * Orchestrates the complete login flow for all authentication types.
   *
   * This is the single source of truth for login, ensuring:
   * 1. User exists in database (created or updated)
   * 2. Roles are synchronized based on origin
   * 3. Session is created
   * 4. JWT is minted
   *
   * @param authenticatedLogin normalized authentication data from any auth source
   * @return Result with JWT and roles for the authenticated user
   */
  @Transactional
  public Result onSuccess(AuthenticatedLogin authenticatedLogin) {
    String login = authenticatedLogin.getLogin().toLowerCase();
    String name = authenticatedLogin.getName();
    UserOrigin origin = authenticatedLogin.getOrigin();
    Set<String> targetRoles = authenticatedLogin.getRoles();

    log.info("LoginService: onSuccess: {} (origin: {})", login, origin);

    // Ensure user exists in database
    authorizationService.ensureUserExists(login, name, origin, this.defaultRoles);

    // Sync roles: align database roles with target roles from this authentication source
    syncRoles(login, origin, targetRoles);

    // Create session
    UUID sessionId = sessionService.createSession(login);
    Instant expiresAt = Instant.now().plus(sessionProps.getExpiration());

    // Mint JWT
    String jwt = jwtService.generateToken(login, sessionId.toString(), Date.from(expiresAt));

    // Get final roles from database
    String[] roles = authorizationService.getUserRoles(login).stream()
        .map(Role::name)
        .toArray(String[]::new);

    return new Result(login, jwt, roles, "Login successful");
  }

  /**
   * Synchronizes the roles for a user from a specific origin with target roles.
   *
   * Adds roles present in targetRoles but not in the database,
   * and removes roles from the database that are not in targetRoles
   * (only for roles assigned by this origin).
   *
   * @param login user login name
   * @param origin authentication origin (for filtering which roles to sync)
   * @param targetRoles the set of role names that should be assigned from this origin
   */
  @Transactional
  private void syncRoles(String login, UserOrigin origin, Set<String> targetRoles) {
    List<String> currentOriginRoles;
    try {
      currentOriginRoles = authorizationService.getRolesByOrigin(login, origin);
    } catch (Exception e) {
      log.warn("Could not fetch {}-origin roles for user {}: {}", origin, login, e.getMessage());
      return;
    }

    // Add roles present in target but not in current
    for (String roleName : targetRoles) {
      if (!currentOriginRoles.contains(roleName)) {
        try {
          authorizationService.addUserToRole(roleName, login, origin);
          log.info("Sync roles: added role '{}' to user '{}' (origin: {})", roleName, login, origin);
        } catch (Exception e) {
          log.warn("Sync roles: could not add role '{}' to user '{}': {}", roleName, login, e.getMessage());
        }
      }
    }

    // Remove roles present in current but not in target (only for this origin)
    for (String roleName : currentOriginRoles) {
      if (!targetRoles.contains(roleName)) {
        try {
          authorizationService.removeUserFromRole(roleName, login, origin);
          log.info("Sync roles: removed role '{}' from user '{}' (origin: {})", roleName, login, origin);
        } catch (Exception e) {
          log.warn("Sync roles: could not remove role '{}' from user '{}': {}", roleName, login, e.getMessage());
        }
      }
    }
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
  @Scheduled(fixedDelayString = "#{@sessionProperties.cleanupInterval.toMillis()}")
  public void cleanupSessions() {
    sessionService.cleanupExpiredSessions();
  }

}

