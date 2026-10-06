package org.ohdsi.webapi.security.authc;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.security.authz.AuthorizationService;
import org.ohdsi.webapi.security.session.SessionProperties;
import org.ohdsi.webapi.security.session.SessionService;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class LoginServiceAdminLoginTest {

  @Configuration
  static class AdminLoginTestConfiguration {
    @Bean
    List<String> configuredAdminLogins(@Value("${security.admin-login:}") String[] adminLogins) {
      return Arrays.asList(adminLogins);
    }
  }

  private SessionService sessionService;
  private AuthorizationService authorizationService;
  private JwtService jwtService;
  private SessionProperties sessionProperties;

  @Before
  public void setUp() {
    sessionService = mock(SessionService.class);
    authorizationService = mock(AuthorizationService.class);
    jwtService = mock(JwtService.class);
    sessionProperties = new SessionProperties();

    when(authorizationService.getRolesByOrigin(anyString(), any(UserOrigin.class)))
        .thenReturn(Collections.emptyList());
    when(authorizationService.getUserRoles(anyString())).thenReturn(Collections.emptyList());
    when(sessionService.createSession(anyString())).thenReturn(UUID.randomUUID());
    when(jwtService.generateToken(anyString(), anyString(), any(Date.class))).thenReturn("jwt");
  }

  @Test
  public void matchingLoginReceivesSystemAdminRole() {
    LoginService service = createService(new String[]{"other@example.com", "  ADMIN@EXAMPLE.COM  "});
    when(authorizationService.ensureUserHasRole("admin", "admin@example.com", UserOrigin.SYSTEM))
        .thenReturn(true);

    service.onSuccess(login("Admin@Example.com"));

    verify(authorizationService).ensureUserHasRole("admin", "admin@example.com", UserOrigin.SYSTEM);
  }

  @Test
  public void commaSeparatedPropertyBindsAllLogins() {
    new ApplicationContextRunner()
        .withUserConfiguration(AdminLoginTestConfiguration.class)
        .withPropertyValues("security.admin-login=first@example.com,second@example.com")
        .run(context -> assertEquals(List.of("first@example.com", "second@example.com"),
            context.getBean("configuredAdminLogins", List.class)));
  }

  @Test
  public void nonMatchingLoginDoesNotReceiveAdminRole() {
    LoginService service = createService(new String[]{"admin@example.com", "another@example.com"});

    service.onSuccess(login("other@example.com"));

    verify(authorizationService, never()).ensureUserHasRole(eq("admin"), anyString(), eq(UserOrigin.SYSTEM));
  }

  @Test
  public void emptyConfigurationDisablesAdminGrant() {
    LoginService service = createService(new String[]{""});

    service.onSuccess(login("admin@example.com"));

    verify(authorizationService, never()).ensureUserHasRole(eq("admin"), anyString(), eq(UserOrigin.SYSTEM));
  }

  private LoginService createService(String[] adminLogins) {
    return new LoginService(sessionService, authorizationService, jwtService, List.of(), adminLogins,
        sessionProperties);
  }

  private AuthenticatedLogin login(String login) {
    return AuthenticatedLogin.builder()
        .login(login)
        .name(login)
        .origin(UserOrigin.OIDC)
        .roles(Set.of())
        .build();
  }
}
