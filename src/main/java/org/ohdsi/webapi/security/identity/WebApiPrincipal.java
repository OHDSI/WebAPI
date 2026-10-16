package org.ohdsi.webapi.security.identity;

import java.security.Principal;
import java.util.Objects;
import org.ohdsi.webapi.security.authz.User;

public final class WebApiPrincipal implements Principal {

  public static final long ANONYMOUS_USER_ID = -1L;
  public static final String ANONYMOUS_LOGIN = "anonymous";

  public static final WebApiPrincipal ANONYMOUS =
      new WebApiPrincipal(new User(ANONYMOUS_USER_ID, ANONYMOUS_LOGIN, "Anonymous"));

  private final User user;

  public WebApiPrincipal(User user) {
    this.user = Objects.requireNonNull(user, "user");
  }

  public long getUserId() {
    return user.id();
  }

  public User getUser() {
    return user;
  }

  @Override
  public String getName() {
    return user.login();
  }

  public boolean isAnonymous() {
    return this == ANONYMOUS || user.id() == ANONYMOUS_USER_ID;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof WebApiPrincipal))
      return false;
    WebApiPrincipal that = (WebApiPrincipal) o;
    return user.id().equals(that.user.id());
  }

  @Override
  public int hashCode() {
    return Long.hashCode(user.id());
  }

  @Override
  public String toString() {
    return "WebApiPrincipal[userId=" + user.id() + ", login=" + user.login() + "]";
  }
}
