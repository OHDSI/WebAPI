package org.ohdsi.webapi.security.authc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * The LoginController class groups the different auth controller endpoints, and
 * we do it with inner classes
 * so that we don't have an explosion of classes in the code tree for 1
 * controller per auth. The class is required in order to
 * apply @ConditionalOnProperty annotations.
 * 
 * There may be a concern with the class names and how they would unwind into
 * something like swagger, and that may need to be investigated, but
 * the theory is that the swagger method will be LoginController$Windows.login.
 * All the other controllers will be prefixed with LoginController$
 * 
 */
@RestController
public class LoginController {

  private final LoginService loginSvc;

  public LoginController(LoginService loginSvc) {
    this.loginSvc = loginSvc;
  }

  @GetMapping("/user/refresh")
  public LoginService.Result refresh(Authentication authentication) {
    if (authentication == null) {
      return LoginService.NO_SESSION;
    }
    return loginSvc.extend(authentication);
  }

  @GetMapping("/user/logout")
  public LoginService.Result logout(Authentication authentication, HttpSession session) {
    // Revoke the WebAPI session
    LoginService.Result result = loginSvc.logout(authentication);
    
    // Clear the Spring Security context to remove any cached principals
    SecurityContextHolder.clearContext();
    
    // Invalidate the HTTP session to clear cookies and session data
    if (session != null) {
      session.invalidate();
    }
    
    return result;
  }

  /**
   * Run-as (impersonation) endpoint. Allows a user with admin:run-as permission
   * to log in as another user.
   */
  @PostMapping("/user/runas")
  @PreAuthorize("isPermitted('admin:run-as')")
  public ResponseEntity<LoginService.Result> runAs(
      @RequestParam String login,
      HttpServletResponse response) {
    try {
      LoginService.Result result = loginSvc.runAs(login);
      return ResponseEntity.ok(result);
    } catch (IllegalArgumentException e) {
      response.setHeader("x-auth-error", "User not found");
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new LoginService.Result(null, null, null, "User not found"));
    }
  }

  /**
   * One-Time Code (OTC) authentication controller. Allows redeeming one-time codes for JWT tokens.
   */
  @RestController
  public static class OTC {
    private final OneTimeCodeService oneTimeCodeService;
    private static final Logger log = LoggerFactory.getLogger(OTC.class);

    public OTC(OneTimeCodeService oneTimeCodeService) {
      this.oneTimeCodeService = oneTimeCodeService;
    }

    /**
     * Redeem a one-time code for a JWT token.
     * 
     * @param code the OTC UUID to redeem
     * @return JWT token wrapped in LoginService.Result, or 401 if OTC is invalid/expired/revoked
     */
    @GetMapping(value = "/user/login/otc", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginService.Result> redeemOtc(@RequestParam UUID code) {

      Optional<OneTimeCodeEntity> otcEntity = oneTimeCodeService.validateAndConsume(code);

      if (otcEntity.isEmpty()) {
        log.debug("OTC redemption failed: code {} invalid or expired", code);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new LoginService.Result(null, null, null, "Invalid or expired code"));
      }

      OneTimeCodeEntity otc = otcEntity.get();
      String jwt = otc.getJwtToken();

      log.debug("OTC: {} redeemed successfully for user: {}", code, otc.getLogin());

      // Return the pre-minted JWT directly
      // Client uses this token to establish session with WebAPI
      return ResponseEntity.ok(new LoginService.Result(otc.getLogin(), jwt, null, "OTC redeemed successfully."));
    }
  }

  /**
   * Windows Authentication controller which responds with JWT and login results.
   */
  @RestController
  @ConditionalOnProperty(prefix = "security.auth.windows", name = "enabled", havingValue = "true")
  public static class Windows {
    private final LoginService loginSvc;
    private final org.ohdsi.webapi.security.authz.mapping.WindowsGroupToRoleMapper windowsGroupToRoleMapper;

    public Windows(LoginService loginSvc,
                   org.ohdsi.webapi.security.authz.mapping.ExternalRoleMapService externalRoleMapService) {
      this.loginSvc = loginSvc;
      this.windowsGroupToRoleMapper = new org.ohdsi.webapi.security.authz.mapping.WindowsGroupToRoleMapper(externalRoleMapService);
    }

    @GetMapping("/user/login/windows")
    public LoginService.Result login(Authentication authentication) {
      // Map Windows groups to WebAPI roles
      java.util.Set<String> roles = windowsGroupToRoleMapper.mapGroupsToRoles(
          authentication.getAuthorities());

      AuthenticatedLogin authenticatedLogin = AuthenticatedLogin.builder()
          .login(authentication.getName())
          .name(authentication.getName())
          .origin(UserOrigin.WINDOWS)
          .roles(roles)
          .originAuthentication(authentication)
          .build();

      return loginSvc.onSuccess(authenticatedLogin);
    }
  }

  /**
   * Database Authentication controller which responds with JWT and login results.
   */
  @RestController
  @ConditionalOnProperty(prefix = "security.auth.db", name = "enabled", havingValue = "true")
  public static class Database {
    private final LoginService loginSvc;
    private final AuthenticationManager dbAuthenticationManager;

    public Database(LoginService loginSvc,
        @Qualifier("dbAuthenticationManager") AuthenticationManager dbAuthenticationManager) {
      this.loginSvc = loginSvc;
      this.dbAuthenticationManager = dbAuthenticationManager;
    }

    @GetMapping("/user/login/db")
    public LoginService.Result login(Authentication authentication) {
      AuthenticatedLogin authenticatedLogin = AuthenticatedLogin.builder()
          .login(authentication.getName())
          .name(authentication.getName())
          .origin(UserOrigin.DATABASE)
          .roles(java.util.Collections.emptySet())
          .originAuthentication(authentication)
          .build();
      return loginSvc.onSuccess(authenticatedLogin);
    }

    @PostMapping(value = "/user/login/db", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> loginPost(
        @RequestParam(required = false) String login,
        @RequestParam(required = false) String password) {
      if (login == null || password == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new LoginService.Result(null, null, null, "Missing credentials"));
      }
      try {
        Authentication auth = dbAuthenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(login, password));
        AuthenticatedLogin authenticatedLogin = AuthenticatedLogin.builder()
            .login(auth.getName())
            .name(auth.getName())
            .origin(UserOrigin.DATABASE)
            .roles(java.util.Collections.emptySet())
            .originAuthentication(auth)
            .build();
        return ResponseEntity.ok(loginSvc.onSuccess(authenticatedLogin));
      } catch (AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new LoginService.Result(null, null, null, "Invalid credentials"));
      }
    }
  }

  /**
   * Database Authentication controller which responds with JWT and login results.
   */
  @RestController
  @ConditionalOnProperty(prefix = "security.auth.ldap", name = "enabled", havingValue = "true")
  public static class Ldap {

    private final LoginService loginSvc;
    private final org.ohdsi.webapi.security.authz.mapping.LdapGroupToRoleMapper ldapGroupToRoleMapper;
    private static final Logger log = LoggerFactory.getLogger(Ldap.class);
    
    public Ldap(LoginService loginSvc,
               org.ohdsi.webapi.security.authz.mapping.ExternalRoleMapService externalRoleMapService) {
      this.loginSvc = loginSvc;
      this.ldapGroupToRoleMapper = new org.ohdsi.webapi.security.authz.mapping.LdapGroupToRoleMapper(externalRoleMapService);
    }

    @GetMapping("/user/login/ldap")
    public LoginService.Result login(Authentication authentication) {

      List<String> groupNames = authentication.getAuthorities().stream()
          .map(org.springframework.security.core.GrantedAuthority::getAuthority)
          .toList();

      log.info("User {} has LDAP groups {}", authentication.getName(), groupNames);

      // Map LDAP groups to WebAPI roles
      java.util.Set<String> roles = ldapGroupToRoleMapper.mapGroupsToRoles(
          authentication.getAuthorities(),
          org.ohdsi.webapi.security.provisioning.model.LdapProviderType.LDAP);

      AuthenticatedLogin authenticatedLogin = AuthenticatedLogin.builder()
          .login(authentication.getName())
          .name(authentication.getName())
          .origin(UserOrigin.LDAP)
          .roles(roles)
          .originAuthentication(authentication)
          .build();

      return loginSvc.onSuccess(authenticatedLogin);
    }
  }
}
