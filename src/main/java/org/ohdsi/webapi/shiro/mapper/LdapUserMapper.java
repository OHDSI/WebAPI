package org.ohdsi.webapi.shiro.mapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LdapUserMapper extends UserMapper {
    @Value("${security.ldap.userMapping.firstname}")
    private String firstnameKey;

    @Value("${security.ldap.userMapping.middlename}")
    private String middlenameKey;

    @Value("${security.ldap.userMapping.lastname}")
    private String lastnameKey;

    @Value("${security.ldap.userMapping.username}")
    private String usernameKey;

    @Override
    public String getFirstnameKey() {
        return firstnameKey;
    }

    @Override
    public String getMiddlenameKey() {
        return middlenameKey;
    }

    @Override
    public String getLastnameKey() {
        return lastnameKey;
    }

    @Override
    public String getUsernameKey() {
        return usernameKey;
    }
}