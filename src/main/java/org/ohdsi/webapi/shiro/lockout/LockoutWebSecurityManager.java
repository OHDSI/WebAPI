package org.ohdsi.webapi.shiro.lockout;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LockoutWebSecurityManager extends DefaultWebSecurityManager {

    private static final Logger log = LoggerFactory.getLogger(LockoutWebSecurityManager.class);
    private LockoutPolicy lockoutPolicy;

    public LockoutWebSecurityManager(LockoutPolicy lockoutPolicy) {

        this.lockoutPolicy = lockoutPolicy;
    }

    @Override
    protected void onFailedLogin(AuthenticationToken token, AuthenticationException ae, Subject subject) {
        log.debug("Failed to login: {}", ae.getMessage(), ae);
        super.onFailedLogin(token, ae, subject);
        if (token instanceof UsernamePasswordToken) {
            String username = ((UsernamePasswordToken) token).getUsername();
            lockoutPolicy.loginFailed(username);
        }
    }

    @Override
    protected void onSuccessfulLogin(AuthenticationToken token, AuthenticationInfo info, Subject subject) {

        super.onSuccessfulLogin(token, info, subject);
        if (token instanceof UsernamePasswordToken) {
            String username = ((UsernamePasswordToken) token).getUsername();
            lockoutPolicy.loginSucceeded(username);
        }
    }

    @Override
    public Subject login(Subject subject, AuthenticationToken token) throws AuthenticationException {

        AuthenticationInfo info;

        if (token instanceof UsernamePasswordToken) {
            String username = ((UsernamePasswordToken) token).getUsername();
            if (lockoutPolicy.isLockedOut(username)) {
                long expiration = lockoutPolicy.getLockExpiration(username);
                long now = new Date().getTime();
                long tryInSeconds = TimeUnit.MILLISECONDS.toSeconds(expiration - now);
                AuthenticationException ae = new LockedAccountException("Maximum login attempts is reached. Please, try again in " + tryInSeconds + " seconds.");
                try {
                    onFailedLogin(token, ae, subject);
                } catch (Exception e) {
                    log.info("onFailure method threw an exception.", e);
                }
                throw ae;
            }
        }
        info = authenticate(token);
        if (Objects.isNull(info) || Objects.isNull(info.getCredentials())) {
            AuthenticationException ae = new AuthenticationException("No authentication info for token [" + token + "]");
            try {
                onFailedLogin(token, ae, subject);
            } catch (Exception e) {
                if (log.isInfoEnabled()) {
                    log.info("onFailedLogin method threw an " +
                            "exception.  Logging and propagating original AuthenticationException.", e);
                }
            }
            throw ae;
        }

        Subject loggedIn = createSubject(token, info, subject);

        onSuccessfulLogin(token, info, loggedIn);

        return loggedIn;
    }
}
