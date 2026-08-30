package org.ohdsi.webapi.security.authc.db;

import java.time.Duration;

public class LockoutPolicyProperties {

  private int maxFailedAttempts;
  private Duration lockoutDuration;

  public LockoutPolicyProperties() {
    // default constructor needed for Spring binding
  }

  public int getMaxFailedAttempts() {
    return maxFailedAttempts;
  }

  public void setMaxFailedAttempts(int maxFailedAttempts) {
    this.maxFailedAttempts = maxFailedAttempts;
  }

  public Duration getLockoutDuration() {
    return lockoutDuration;
  }

  public void setLockoutDuration(Duration lockoutDuration) {
    this.lockoutDuration = lockoutDuration;
  }

  @Override
  public String toString() {
    return "LockoutPolicyProperties{" +
        "maxFailedAttempts=" + maxFailedAttempts +
        ", lockoutDuration=" + lockoutDuration +
        '}';
  }
}