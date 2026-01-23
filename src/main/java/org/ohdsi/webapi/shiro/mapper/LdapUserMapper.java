package org.ohdsi.webapi.shiro.mapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LdapUserMapper extends UserMapper {
    @Value("${security.auth.ldap.userMapping.firstnameAttr}")
    private String firstnameKey;

    @Value("${security.auth.ldap.userMapping.middlenameAttr}")
    private String middlenameKey;

    @Value("${security.auth.ldap.userMapping.lastnameAttr}")
    private String lastnameKey;

    @Value("${security.auth.ldap.userMapping.usernameAttr}")
    private String usernameKey;

    @Value("${security.auth.ldap.userMapping.displaynameAttr}")
    private String displaynameKey;

    @Override
    public String getFirstnameAttr() {
        return firstnameKey;
    }

    @Override
    public String getMiddlenameAttr() {
        return middlenameKey;
    }

    @Override
    public String getLastnameAttr() {
        return lastnameKey;
    }

    @Override
    public String getUsernameAttr() {
        return usernameKey;
    }

    @Override
    public String getDisplaynameAttr() {
        return displaynameKey;
    }
}