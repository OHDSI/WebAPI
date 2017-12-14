package org.ohdsi.webapi.shiro;

public class LdapAuthFilter extends AbstractLdapAuthFilter<LdapUsernamePasswordToken> {
    @Override
    protected LdapUsernamePasswordToken getToken() {

        return new LdapUsernamePasswordToken();
    }
}
