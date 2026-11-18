package org.ohdsi.webapi.security.authc.assertion;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.ohdsi.webapi.security.authc.WebApiAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Binds a WebAPI session token to a live proxy assertion for the same user.
 *
 * <p>Applies only to requests authenticated with a WebAPI session token. Anonymous
 * requests are left to WebAPI's own authorization, and API-key requests (which
 * carry no session) are not touched, so service-to-service calls keep working.
 */
public class AssertionBindingFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(AssertionBindingFilter.class);

  private final AssertionVerifier verifier;

  public AssertionBindingFilter(AssertionVerifier verifier) {
    this.verifier = verifier;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (!(auth instanceof WebApiAuthenticationToken token) || !token.hasSession() || token.isAnonymous()) {
      chain.doFilter(request, response);
      return;
    }

    String tokenLogin = token.getName();
    switch (verifier.verify(request)) {
      case AssertionVerifier.Verified v when v.login().equalsIgnoreCase(tokenLogin) ->
          chain.doFilter(request, response);
      case AssertionVerifier.Verified v -> {
        log.warn("SECURITY_AUDIT: token for '{}' presented with assertion for '{}'", tokenLogin, v.login());
        reject(response, HttpServletResponse.SC_UNAUTHORIZED, "Assertion does not match the session user");
      }
      case AssertionVerifier.Unavailable u ->
          reject(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Identity provider unavailable");
      case AssertionVerifier.Missing m ->
          reject(response, HttpServletResponse.SC_UNAUTHORIZED, "Assertion required");
      case AssertionVerifier.Invalid i ->
          reject(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid assertion");
    }
  }

  private static void reject(HttpServletResponse response, int status, String message) throws IOException {
    response.setStatus(status);
    response.setHeader("x-auth-error", message);
    response.setContentType("application/json");
    response.getWriter().write("{\"message\":\"" + message + "\"}");
  }
}
