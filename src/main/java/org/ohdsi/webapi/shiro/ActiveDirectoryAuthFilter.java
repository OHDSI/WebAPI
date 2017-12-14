package org.ohdsi.webapi.shiro;

public class ActiveDirectoryAuthFilter extends AbstractLdapAuthFilter<ActiveDirectoryUsernamePasswordToken> {
    @Override
    protected ActiveDirectoryUsernamePasswordToken getToken() {

        return new ActiveDirectoryUsernamePasswordToken();
    }
}
