package org.ohdsi.webapi.shiro.mapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ADUserMapper extends UserMapper {
    @Value("${security.auth.ad.userMapping.firstnameAttr}")
    private String firstnameKey;

    @Value("${security.auth.ad.userMapping.middlenameAttr}")
    private String middlenameKey;

    @Value("${security.auth.ad.userMapping.lastnameAttr}")
    private String lastnameKey;

    @Value("${security.auth.ad.userMapping.usernameAttr}")
    private String usernameKey;

    @Value("${security.auth.ad.userMapping.displaynameAttr}")
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