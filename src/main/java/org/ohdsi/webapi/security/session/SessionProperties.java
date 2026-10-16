package org.ohdsi.webapi.security.session;

import java.time.Duration;

public class SessionProperties {

  private boolean singleLogin = true;
  private Duration expiration = Duration.ofHours(8);
  private Duration cleanupInterval = Duration.ofDays(1);

  public boolean isSingleLogin() {
    return singleLogin;
  }

  public void setSingleLogin(boolean singleLogin) {
    this.singleLogin = singleLogin;
  }

  public Duration getExpiration() {
    return expiration;
  }

  public void setExpiration(Duration expiration) {
    this.expiration = expiration;
  }

  public Duration getCleanupInterval() {
    return cleanupInterval;
  }

  public void setCleanupInterval(Duration cleanupInterval) {
    this.cleanupInterval = cleanupInterval;
  }

  
}
