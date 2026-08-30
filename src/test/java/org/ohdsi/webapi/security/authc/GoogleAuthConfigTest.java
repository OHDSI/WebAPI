package org.ohdsi.webapi.security.authc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.web.filter.ForwardedHeaderFilter;

import jakarta.servlet.http.HttpServletRequest;

public class GoogleAuthConfigTest {

  @Test
  public void googleRedirectUriUsesHttpsWhenForwardedHeadersAreProcessed() throws Exception {
    OAuth2AuthorizationRequest authorizationRequest = resolveAuthorizationRequest(true);

    assertNotNull(authorizationRequest);
    assertEquals(
        "https://atlas-preview.ohdsi.org/WebAPI/user/oauth/callback/google",
        authorizationRequest.getRedirectUri());
    assertTrue(authorizationRequest.getAuthorizationRequestUri().contains(
        "redirect_uri=https%3A%2F%2Fatlas-preview.ohdsi.org%2FWebAPI%2Fuser%2Foauth%2Fcallback%2Fgoogle"));
  }

  @Test
  public void googleRedirectUriFallsBackToHttpWhenForwardedHeadersAreIgnored() throws Exception {
    OAuth2AuthorizationRequest authorizationRequest = resolveAuthorizationRequest(false);

    assertNotNull(authorizationRequest);
    assertEquals(
        "http://atlas-preview.ohdsi.org/WebAPI/user/oauth/callback/google",
        authorizationRequest.getRedirectUri());
    assertTrue(authorizationRequest.getAuthorizationRequestUri().contains(
        "redirect_uri=http%3A%2F%2Fatlas-preview.ohdsi.org%2FWebAPI%2Fuser%2Foauth%2Fcallback%2Fgoogle"));
  }

  private OAuth2AuthorizationRequest resolveAuthorizationRequest(boolean applyForwardedHeaderFilter) throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/WebAPI/user/login/google");
    request.setContextPath("/WebAPI");
    request.setServletPath("/user/login/google");
    request.setRequestURI("/WebAPI/user/login/google");
    request.setScheme("http");
    request.setServerName("atlas-preview.ohdsi.org");
    request.setServerPort(8080);
    request.setSecure(false);
    request.addHeader("Host", "atlas-preview.ohdsi.org");
    request.addHeader("X-Forwarded-Proto", "https");
    request.addHeader("X-Forwarded-Host", "atlas-preview.ohdsi.org");
    request.addHeader("X-Forwarded-Port", "443");

    HttpServletRequest requestToResolve = request;
    if (applyForwardedHeaderFilter) {
      AtomicReference<HttpServletRequest> forwardedRequest = new AtomicReference<>();
      new ForwardedHeaderFilter().doFilter(request, new MockHttpServletResponse(), (filteredRequest, filteredResponse) ->
          forwardedRequest.set((HttpServletRequest) filteredRequest));
      requestToResolve = forwardedRequest.get();
    }

    ClientRegistrationRepository repository = new InMemoryClientRegistrationRepository(googleClientRegistration());
    return GoogleAuthConfig.googleAuthorizationRequestResolver(repository).resolve(requestToResolve);
  }

  private ClientRegistration googleClientRegistration() {
    return ClientRegistration.withRegistrationId("google")
        .clientId("client-id")
        .clientSecret("client-secret")
        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
        .redirectUri("{baseUrl}/user/oauth/callback/{registrationId}")
        .scope("openid", "profile", "email")
        .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
        .tokenUri("https://oauth2.googleapis.com/token")
        .userInfoUri("https://openidconnect.googleapis.com/v1/userinfo")
        .userNameAttributeName("sub")
        .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
        .clientName("Google")
        .build();
  }
}
