package org.ohdsi.webapi.security.authc.assertion;

import java.util.Collections;

import jakarta.servlet.http.HttpServletRequest;

import org.ohdsi.webapi.security.authc.AuthenticatedLogin;
import org.ohdsi.webapi.security.authc.LoginService;
import org.ohdsi.webapi.security.authc.UserOrigin;
import org.ohdsi.webapi.security.authz.AuthorizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code GET /user/login/assertion}: exchange a verified proxy assertion for a
 * WebAPI session and token, through the same {@link LoginService} as every other
 * provider. Responds like the other login endpoints, so clients that support an
 * "ajax" provider (no credentials form) need nothing special.
 */
@RestController
@ConditionalOnProperty(prefix = "security.auth.assertion", name = "enabled", havingValue = "true")
public class AssertionLoginController {

  private static final Logger log = LoggerFactory.getLogger(AssertionLoginController.class);

  private final AssertionVerifier verifier;
  private final LoginService loginService;
  private final AuthorizationService authorizationService;
  private final AssertionAuthProperties props;

  public AssertionLoginController(AssertionVerifier verifier, LoginService loginService,
      AuthorizationService authorizationService, AssertionAuthProperties props) {
    this.verifier = verifier;
    this.loginService = loginService;
    this.authorizationService = authorizationService;
    this.props = props;
  }

  @GetMapping(value = AssertionAuthConfig.LOGIN_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<LoginService.Result> login(HttpServletRequest request) {
    return switch (verifier.verify(request)) {
      case AssertionVerifier.Verified v -> loginAs(v);
      case AssertionVerifier.Missing m -> refuse(HttpStatus.UNAUTHORIZED, "Assertion required");
      case AssertionVerifier.Invalid i -> refuse(HttpStatus.UNAUTHORIZED, "Invalid assertion");
      case AssertionVerifier.Unavailable u -> refuse(HttpStatus.SERVICE_UNAVAILABLE, "Identity provider unavailable");
    };
  }

  private ResponseEntity<LoginService.Result> loginAs(AssertionVerifier.Verified v) {
    if (!props.isAutoProvision() && authorizationService.getUserByLogin(v.login()).isEmpty()) {
      log.info("Assertion login refused for unknown user '{}' (auto-provision off)", v.login());
      return refuse(HttpStatus.FORBIDDEN, "User is not registered");
    }

    AuthenticatedLogin login = AuthenticatedLogin.builder()
        .login(v.login())
        .name(v.name())
        .origin(UserOrigin.ASSERTION)
        // Assertions carry no roles; roles are managed in WebAPI.
        .roles(Collections.emptySet())
        .attribute("assertion.issuer", v.jwt().getIssuer() == null ? null : v.jwt().getIssuer().toString())
        .build();
    return ResponseEntity.ok(loginService.onSuccess(login));
  }

  private static ResponseEntity<LoginService.Result> refuse(HttpStatus status, String message) {
    return ResponseEntity.status(status)
        .header("x-auth-error", message)
        .body(new LoginService.Result(null, null, null, message));
  }
}
