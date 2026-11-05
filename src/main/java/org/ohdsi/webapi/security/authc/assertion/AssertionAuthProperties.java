package org.ohdsi.webapi.security.authc.assertion;

import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Settings for the trusted-assertion provider ({@code security.auth.assertion.*}).
 *
 * <p>The provider authenticates a user from a short-lived signed JWT that a
 * reverse proxy adds to every request after its own authentication (for example
 * nginx {@code auth_request} in front of an SSO service). WebAPI verifies the
 * signature against a JWKS and pins the issuer and audience, so the header can't
 * be forged by a client, even one that reaches WebAPI directly.
 */
@ConfigurationProperties(prefix = "security.auth.assertion")
public class AssertionAuthProperties {

  /** Turns the provider on. Off by default. */
  private boolean enabled = false;

  /** Request header that carries the assertion. */
  private String header = "sso-assertion";

  /** JWKS endpoint holding the proxy's public signing keys. Required when enabled. */
  private String jwkSetUri;

  /** Exact {@code iss} the assertion must carry. Required when enabled. */
  private String issuer;

  /** Value the assertion's {@code aud} must contain. Required when enabled. */
  private String audience;

  /** Accepted JWS algorithms. Anything else, including HS* and none, is refused. */
  private List<String> algorithms = List.of("ES256");

  /** Allowed clock difference when checking {@code exp} and {@code nbf}. */
  private Duration clockSkew = Duration.ofSeconds(30);

  /** Connect and read timeout for fetching the JWKS. */
  private Duration jwksTimeout = Duration.ofSeconds(5);

  /** Claim holding the WebAPI login. */
  private String usernameClaim = "sub";

  /** Claims used to build the display name; the login is used when both are empty. */
  private String givenNameClaim = "given_name";
  private String familyNameClaim = "family_name";

  /** Create a WebAPI user on first login. When false, unknown users get 403. */
  private boolean autoProvision = true;

  /**
   * Require a valid assertion for the same user on every request made with a
   * WebAPI session token, not just at login. A leaked WebAPI token is then useless
   * without a live proxy session, and signing out at the proxy takes effect within
   * the assertion's lifetime. API-key requests are not affected.
   */
  private boolean enforceOnEveryRequest = true;

  /** Name shown for this provider in {@code /auth/providers}. */
  private String providerName = "Single Sign-On";

  /** Logout URL handed to the client, e.g. the proxy's own sign-out page. */
  private String logoutUrl;

  public boolean isEnabled() { return enabled; }
  public void setEnabled(boolean enabled) { this.enabled = enabled; }
  public String getHeader() { return header; }
  public void setHeader(String header) { this.header = header; }
  public String getJwkSetUri() { return jwkSetUri; }
  public void setJwkSetUri(String jwkSetUri) { this.jwkSetUri = jwkSetUri; }
  public String getIssuer() { return issuer; }
  public void setIssuer(String issuer) { this.issuer = issuer; }
  public String getAudience() { return audience; }
  public void setAudience(String audience) { this.audience = audience; }
  public List<String> getAlgorithms() { return algorithms; }
  public void setAlgorithms(List<String> algorithms) { this.algorithms = algorithms; }
  public Duration getClockSkew() { return clockSkew; }
  public void setClockSkew(Duration clockSkew) { this.clockSkew = clockSkew; }
  public Duration getJwksTimeout() { return jwksTimeout; }
  public void setJwksTimeout(Duration jwksTimeout) { this.jwksTimeout = jwksTimeout; }
  public String getUsernameClaim() { return usernameClaim; }
  public void setUsernameClaim(String usernameClaim) { this.usernameClaim = usernameClaim; }
  public String getGivenNameClaim() { return givenNameClaim; }
  public void setGivenNameClaim(String givenNameClaim) { this.givenNameClaim = givenNameClaim; }
  public String getFamilyNameClaim() { return familyNameClaim; }
  public void setFamilyNameClaim(String familyNameClaim) { this.familyNameClaim = familyNameClaim; }
  public boolean isAutoProvision() { return autoProvision; }
  public void setAutoProvision(boolean autoProvision) { this.autoProvision = autoProvision; }
  public boolean isEnforceOnEveryRequest() { return enforceOnEveryRequest; }
  public void setEnforceOnEveryRequest(boolean enforceOnEveryRequest) { this.enforceOnEveryRequest = enforceOnEveryRequest; }
  public String getProviderName() { return providerName; }
  public void setProviderName(String providerName) { this.providerName = providerName; }
  public String getLogoutUrl() { return logoutUrl; }
  public void setLogoutUrl(String logoutUrl) { this.logoutUrl = logoutUrl; }
}
