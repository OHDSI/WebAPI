package org.ohdsi.webapi.security.authc;

import org.springframework.security.core.Authentication;
import org.ohdsi.webapi.security.authc.UserOrigin;

import java.util.*;

/**
 * Normalized authentication login object that bridges all authentication methods
 * (Database, LDAP, Windows, OIDC) into a common structure for the login pipeline.
 *
 * This class is used by all authentication handlers to standardize the data passed
 * to LoginService.onSuccess(), ensuring consistent user creation, role mapping,
 * session establishment, and JWT generation across all authentication types.
 */
public class AuthenticatedLogin {

  private final String login;
  private final String name;
  private final UserOrigin origin;
  private final Set<String> roles;
  private final Authentication originAuthentication;
  private final Map<String, Object> attributes;

  private AuthenticatedLogin(Builder builder) {
    this.login = Objects.requireNonNull(builder.login, "login cannot be null");
    this.name = Objects.requireNonNull(builder.name, "name cannot be null");
    if (this.login.isBlank()) {
      throw new IllegalArgumentException("login cannot be blank");
    }
    if (this.name.isBlank()) {
      throw new IllegalArgumentException("name cannot be blank");
    }
    this.origin = Objects.requireNonNull(builder.origin, "origin cannot be null");
    this.roles = builder.roles != null ? new HashSet<>(builder.roles) : new HashSet<>();
    this.originAuthentication = builder.originAuthentication;
    this.attributes = builder.attributes != null ? new HashMap<>(builder.attributes) : new HashMap<>();
  }

  /**
   * Gets the normalized login name (typically lowercase).
   */
  public String getLogin() {
    return login;
  }

  /**
   * Gets the display name for the user.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the origin/source of this authentication.
   */
  public UserOrigin getOrigin() {
    return origin;
  }

  /**
   * Gets the set of WebAPI role names that should be assigned to this user.
   * These roles should already be filtered to only valid WebAPI roles
   * and mapped from the authentication source (e.g., LDAP groups, OIDC claims).
   */
  public Set<String> getRoles() {
    return Collections.unmodifiableSet(roles);
  }

  /**
   * Gets the original Spring Authentication object for debugging/auditing purposes.
   * May be null if not provided by the authentication handler.
   */
  public Authentication getOriginAuthentication() {
    return originAuthentication;
  }

  /**
   * Gets optional auth-type-specific attributes.
   * Can be used to pass additional data through the login pipeline.
   */
  public Map<String, Object> getAttributes() {
    return Collections.unmodifiableMap(attributes);
  }

  /**
   * Gets an attribute by key, or null if not present.
   */
  public Object getAttribute(String key) {
    return attributes.get(key);
  }

  /**
   * Creates a new builder for constructing AuthenticatedLogin instances.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder for constructing AuthenticatedLogin instances.
   */
  public static class Builder {
    private String login;
    private String name;
    private UserOrigin origin;
    private Set<String> roles;
    private Authentication originAuthentication;
    private Map<String, Object> attributes;

    /**
     * Sets the login name.
     */
    public Builder login(String login) {
      this.login = login;
      return this;
    }

    /**
     * Sets the display name.
     */
    public Builder name(String name) {
      this.name = name;
      return this;
    }

    /**
     * Sets the authentication origin.
     */
    public Builder origin(UserOrigin origin) {
      this.origin = origin;
      return this;
    }

    /**
     * Sets the roles. If called multiple times, the last value is used.
     */
    public Builder roles(Set<String> roles) {
      this.roles = roles;
      return this;
    }

    /**
     * Adds a single role. Can be called multiple times to build up the role set.
     */
    public Builder addRole(String role) {
      if (this.roles == null) {
        this.roles = new HashSet<>();
      }
      this.roles.add(role);
      return this;
    }

    /**
     * Sets the original Spring Authentication object.
     */
    public Builder originAuthentication(Authentication originAuthentication) {
      this.originAuthentication = originAuthentication;
      return this;
    }

    /**
     * Sets auth-type-specific attributes.
     */
    public Builder attributes(Map<String, Object> attributes) {
      this.attributes = attributes;
      return this;
    }

    /**
     * Adds a single attribute. Can be called multiple times.
     */
    public Builder attribute(String key, Object value) {
      if (this.attributes == null) {
        this.attributes = new HashMap<>();
      }
      this.attributes.put(key, value);
      return this;
    }

    /**
     * Builds the AuthenticatedLogin instance.
     */
    public AuthenticatedLogin build() {
      return new AuthenticatedLogin(this);
    }
  }

  @Override
  public String toString() {
    return "AuthenticatedLogin{" +
        "login='" + login + '\'' +
        ", name='" + name + '\'' +
        ", origin=" + origin +
        ", roles=" + roles +
        ", attributes=" + attributes +
        '}';
  }
}
