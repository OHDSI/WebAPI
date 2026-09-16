package org.ohdsi.webapi.security.authc;

import org.ohdsi.webapi.security.identity.WebApiPrincipal;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an authenticated (or anonymous) WebAPI request.
 * 
 * - The principal is always a WebApiPrincipal.
 * - Session ID is optional (null for anonymous contexts).
 * - Authorities can be empty for anonymous or assigned as needed.
 */
public final class WebApiAuthenticationToken extends AbstractAuthenticationToken {

  private final WebApiPrincipal principal;
  private final UUID sessionId;

  private WebApiAuthenticationToken(
      WebApiPrincipal principal,
      UUID sessionId,
      Collection<? extends GrantedAuthority> authorities) {

    super(authorities);
    this.principal = Objects.requireNonNull(principal, "principal");
    this.sessionId = sessionId;
    setAuthenticated(true);
  }

  /**
   * Create an authenticated token for a real user.
   */
  public static WebApiAuthenticationToken authenticated(
      WebApiPrincipal principal,
      UUID sessionId,
      Collection<? extends GrantedAuthority> authorities) {

    return new WebApiAuthenticationToken(principal, sessionId, authorities);
  }

  /**
   * Create an anonymous token.
   * Uses the canonical WebApiPrincipal.ANONYMOUS.
   * Session ID is always null.
   */
  public static WebApiAuthenticationToken anonymous() {
    return new WebApiAuthenticationToken(WebApiPrincipal.ANONYMOUS, null, List.of());
  }

  @Override
  public WebApiPrincipal getPrincipal() {
    return principal;
  }

  @Override
  public Object getCredentials() {
    return null;
  }

  /**
   * Returns the session ID associated with this authentication.
   * May be null for anonymous contexts.
   */
  public UUID getSessionId() {
    return sessionId;
  }

  /**
   * Returns true if a session ID is present.
   */
  public boolean hasSession() {
    return sessionId != null;
  }

  /**
   * Returns true if this authentication represents an anonymous context.
   */
  public boolean isAnonymous() {
    return principal.isAnonymous();
  }
}
