package org.ohdsi.webapi.shiro.lockout;

public class NoLockoutPolicy implements LockoutPolicy {
    @Override
    public boolean isLockedOut(String principal) {

        return true;
    }

    @Override
    public long getLockExpiration(String principal) {

        return 0;
    }

    @Override
    public void loginFailed(String principal) {

    }

    @Override
    public void loginSuceeded(String principal) {

    }
}
