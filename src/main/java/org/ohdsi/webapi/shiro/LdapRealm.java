package org.ohdsi.webapi.shiro;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.realm.ldap.JndiLdapRealm;

public class LdapRealm extends JndiLdapRealm {

    @Override
    public boolean supports(AuthenticationToken token) {

        return token instanceof LdapUsernamePasswordToken;
    }
}
