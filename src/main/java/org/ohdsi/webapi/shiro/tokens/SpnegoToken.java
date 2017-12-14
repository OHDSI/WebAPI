package org.ohdsi.webapi.shiro.tokens;

import org.apache.shiro.authc.AuthenticationToken;

public class SpnegoToken implements AuthenticationToken {

    byte[] token;

    public SpnegoToken(byte[] token) {

        this.token = token;
    }

    @Override
    public Object getPrincipal() {

        return null;
    }

    @Override
    public Object getCredentials() {

        return token;
    }
}
