package org.ohdsi.webapi.security.identity;

import java.security.Principal;
import java.util.Objects;

public final class WebApiPrincipal implements Principal {

  public static final long ANONYMOUS_USER_ID = -1L;
  public static final String ANONYMOUS_LOGIN = "anonymous";

  public static final WebApiPrincipal ANONYMOUS = new WebApiPrincipal(ANONYMOUS_USER_ID, ANONYMOUS_LOGIN);

  private final long userId;
  private final String login;

  public WebApiPrincipal(long userId, String login) {
    this.userId = userId;
    this.login = Objects.requireNonNull(login, "login");
  }

  public long getUserId() {
    return userId;
  }

  @Override
  public String getName() {
    return login;
  }

  public boolean isAnonymous() {
    return this == ANONYMOUS || userId == ANONYMOUS_USER_ID;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof WebApiPrincipal))
      return false;
    WebApiPrincipal that = (WebApiPrincipal) o;
    return userId == that.userId;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(userId);
  }

  @Override
  public String toString() {
    return "WebApiPrincipal[userId=" + userId + ", login=" + login + "]";
  }
}
