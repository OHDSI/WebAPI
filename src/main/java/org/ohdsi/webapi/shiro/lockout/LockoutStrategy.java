package org.ohdsi.webapi.shiro.lockout;

public interface LockoutStrategy {
    long getLockDuration(int attempts);
}
