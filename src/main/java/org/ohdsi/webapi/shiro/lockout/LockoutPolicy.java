package org.ohdsi.webapi.shiro.lockout;

public interface LockoutPolicy {
    boolean isLockedOut(String principal);
    long getLockExpiration(String principal);
    void loginFailed(String principal);
    void loginSuceeded(String principal);
}
