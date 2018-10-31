package org.ohdsi.webapi.shiro.lockout;

public interface LockoutPolicy {
    boolean isLockedOut(String principal);
    boolean isLockedOut(LockEntry entry);
    long getLockExpiration(String principal);
    void loginFailed(String principal);
    void loginSucceeded(String principal);
}
