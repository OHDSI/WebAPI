package org.ohdsi.webapi.security.authc.assertion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.security.authc.WebApiAuthenticationToken;
import org.ohdsi.webapi.security.identity.WebApiPrincipal;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class AssertionBindingFilterTest {

  private AssertionFixture fixture;
  private AssertionBindingFilter filter;

  @Before
  public void setUp() throws Exception {
    fixture = new AssertionFixture();
    filter = new AssertionBindingFilter(fixture.verifier());
  }

  @After
  public void tearDown() {
    SecurityContextHolder.clearContext();
    fixture.close();
  }

  private static void authenticateAs(String login, UUID sessionId) {
    WebApiPrincipal principal = mock(WebApiPrincipal.class);
    when(principal.getName()).thenReturn(login);
    when(principal.isAnonymous()).thenReturn(false);
    SecurityContextHolder.getContext().setAuthentication(
        WebApiAuthenticationToken.authenticated(principal, sessionId, List.of()));
  }

  /** Runs the filter; returns the response, with the chain's request recorded. */
  private MockHttpServletResponse run(String assertion, MockFilterChain chain) throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/cohortdefinition");
    if (assertion != null) {
      request.addHeader("sso-assertion", assertion);
    }
    MockHttpServletResponse response = new MockHttpServletResponse();
    filter.doFilter(request, response, chain);
    return response;
  }

  @Test
  public void sessionTokenWithMatchingAssertionPasses() throws Exception {
    authenticateAs("ada", UUID.randomUUID());
    MockFilterChain chain = new MockFilterChain();

    MockHttpServletResponse response = run(fixture.sign(AssertionFixture.claims("ADA").build()), chain);

    assertEquals(200, response.getStatus());
    assertNotNull("request reached the application", chain.getRequest());
  }

  @Test
  public void sessionTokenWithoutAssertionIs401() throws Exception {
    authenticateAs("ada", UUID.randomUUID());
    MockFilterChain chain = new MockFilterChain();

    assertEquals(401, run(null, chain).getStatus());
    assertNull(chain.getRequest());
  }

  @Test
  public void sessionTokenWithAnotherUsersAssertionIs401() throws Exception {
    authenticateAs("ada", UUID.randomUUID());
    MockFilterChain chain = new MockFilterChain();

    assertEquals(401, run(fixture.sign(AssertionFixture.claims("mallory").build()), chain).getStatus());
    assertNull(chain.getRequest());
  }

  @Test
  public void sessionTokenWithInvalidAssertionIs401() throws Exception {
    authenticateAs("ada", UUID.randomUUID());
    MockFilterChain chain = new MockFilterChain();

    assertEquals(401, run(AssertionFixture.unsigned(AssertionFixture.claims("ada").build()), chain).getStatus());
    assertNull(chain.getRequest());
  }

  @Test
  public void unreachableJwksIs503() throws Exception {
    String token = fixture.sign(AssertionFixture.claims("ada").build());
    fixture.close();
    authenticateAs("ada", UUID.randomUUID());
    MockFilterChain chain = new MockFilterChain();

    assertEquals(503, run(token, chain).getStatus());
    assertNull(chain.getRequest());
  }

  @Test
  public void apiKeyRequestWithoutSessionIsNotChecked() throws Exception {
    authenticateAs("service", null);
    MockFilterChain chain = new MockFilterChain();

    assertEquals(200, run(null, chain).getStatus());
    assertNotNull(chain.getRequest());
  }

  @Test
  public void anonymousRequestIsNotChecked() throws Exception {
    SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
        "key", "anonymous", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
    MockFilterChain chain = new MockFilterChain();

    assertEquals(200, run(null, chain).getStatus());
    assertNotNull(chain.getRequest());
  }

  @Test
  public void unauthenticatedRequestIsNotChecked() throws Exception {
    MockFilterChain chain = new MockFilterChain();

    assertEquals(200, run(null, chain).getStatus());
    assertNotNull(chain.getRequest());
  }
}
