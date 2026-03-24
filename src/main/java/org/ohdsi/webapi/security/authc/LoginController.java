package org.ohdsi.webapi.security.authc;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

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
  public LoginService.Result logout(Authentication authentication) {
    return loginSvc.logout(authentication);
  }  

  /**
   * Windows Authentication controller which responds with JWT and login results.
   */
  @RestController
  @ConditionalOnProperty(prefix = "security.auth.windows", name = "enabled", havingValue = "true")
  public static class Windows {
    private final LoginService loginSvc;

    public Windows(LoginService loginSvc) {
      this.loginSvc = loginSvc;
    }

    @GetMapping("/user/login/windows")
    public LoginService.Result login(Authentication authentication) {
      return loginSvc.onSuccess(authentication);
    }
  }

  /**
   * Database Authentication controller which responds with JWT and login results.
   */
  @RestController
  @ConditionalOnProperty(prefix = "security.auth.db", name = "enabled", havingValue = "true")
  public static class Database {
    private final LoginService loginSvc;

    public Database(LoginService loginSvc) {
      this.loginSvc = loginSvc;
    }

    @GetMapping("/user/login/db")
    public LoginService.Result login(Authentication authentication) {
      return loginSvc.onSuccess(authentication);
    }

    @PostMapping("/user/login/db")
    public LoginService.Result loginPost(
        @RequestParam String login,
        @RequestParam String password,
        @Qualifier("dbAuthenticationManager") AuthenticationManager authManager) {
      Authentication auth = authManager.authenticate(
          new UsernamePasswordAuthenticationToken(login, password));
      return loginSvc.onSuccess(auth);
    }
  }

  /**
   * Database Authentication controller which responds with JWT and login results.
   */
  @RestController
  @ConditionalOnProperty(prefix = "security.auth.ldap", name = "enabled", havingValue = "true")
  public static class Ldap {

    private final LoginService loginSvc;
    private static final Logger log = LoggerFactory.getLogger(Ldap.class);
    
    public Ldap(LoginService loginSvc) {
      this.loginSvc = loginSvc;
    }

    @GetMapping("/user/login/ldap")
    public LoginService.Result login(Authentication authentication) {

      List<String> roles = authentication.getAuthorities().stream()
          .map(GrantedAuthority::getAuthority)
          .toList();

      log.info("User {} has roles {}", authentication.getName(), roles);
      return loginSvc.onSuccess(authentication);
    }
  }
}
