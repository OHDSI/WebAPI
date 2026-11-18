package org.ohdsi.webapi.security.authc.assertion;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.ohdsi.webapi.security.authc.AuthenticatedLogin;
import org.ohdsi.webapi.security.authc.LoginService;
import org.ohdsi.webapi.security.authc.UserOrigin;
import org.ohdsi.webapi.security.authz.AuthorizationService;
import org.ohdsi.webapi.security.authz.User;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

public class AssertionLoginControllerTest {

  private AssertionFixture fixture;
  private LoginService loginService;
  private AuthorizationService authorizationService;
  private AssertionAuthProperties props;

  @Before
  public void setUp() throws Exception {
    fixture = new AssertionFixture();
    loginService = mock(LoginService.class);
    authorizationService = mock(AuthorizationService.class);
    props = fixture.props();
    when(loginService.onSuccess(any())).thenReturn(new LoginService.Result("ada", "webapi-jwt", new String[0], "Login successful"));
  }

  @After
  public void tearDown() {
    fixture.close();
  }

  private ResponseEntity<LoginService.Result> login(String token) {
    AssertionLoginController controller = new AssertionLoginController(
        new AssertionVerifier(AssertionAuthConfig.assertionJwtDecoder(props), props),
        loginService, authorizationService, props);
    MockHttpServletRequest request = new MockHttpServletRequest("GET", AssertionAuthConfig.LOGIN_PATH);
    if (token != null) {
      request.addHeader("sso-assertion", token);
    }
    return controller.login(request);
  }

  @Test
  public void validAssertionLogsInThroughLoginService() throws Exception {
    ResponseEntity<LoginService.Result> response = login(fixture.sign(AssertionFixture.claims("Ada").build()));

    assertEquals(200, response.getStatusCode().value());
    assertEquals("webapi-jwt", response.getBody().jwt());

    ArgumentCaptor<AuthenticatedLogin> captor = ArgumentCaptor.forClass(AuthenticatedLogin.class);
    verify(loginService).onSuccess(captor.capture());
    AuthenticatedLogin login = captor.getValue();
    assertEquals("ada", login.getLogin());
    assertEquals("Ada Lovelace", login.getName());
    assertEquals(UserOrigin.ASSERTION, login.getOrigin());
    assertEquals(0, login.getRoles().size());
  }

  @Test
  public void missingAssertionIs401() {
    ResponseEntity<LoginService.Result> response = login(null);
    assertEquals(401, response.getStatusCode().value());
    assertEquals("Assertion required", response.getHeaders().getFirst("x-auth-error"));
    verify(loginService, never()).onSuccess(any());
  }

  @Test
  public void invalidAssertionIs401() throws Exception {
    ResponseEntity<LoginService.Result> response =
        login(fixture.sign(AssertionFixture.claims("ada").audience("other.example.org").build()));
    assertEquals(401, response.getStatusCode().value());
    verify(loginService, never()).onSuccess(any());
  }

  @Test
  public void unreachableJwksIs503() throws Exception {
    String token = fixture.sign(AssertionFixture.claims("ada").build());
    fixture.close();
    assertEquals(503, login(token).getStatusCode().value());
    verify(loginService, never()).onSuccess(any());
  }

  @Test
  public void unknownUserIs403WhenAutoProvisionIsOff() throws Exception {
    props.setAutoProvision(false);
    when(authorizationService.getUserByLogin("ada")).thenReturn(Optional.empty());

    assertEquals(403, login(fixture.sign(AssertionFixture.claims("ada").build())).getStatusCode().value());
    verify(loginService, never()).onSuccess(any());
  }

  @Test
  public void knownUserLogsInWhenAutoProvisionIsOff() throws Exception {
    props.setAutoProvision(false);
    when(authorizationService.getUserByLogin("ada")).thenReturn(Optional.of(mock(User.class)));

    assertEquals(200, login(fixture.sign(AssertionFixture.claims("ada").build())).getStatusCode().value());
  }
}
