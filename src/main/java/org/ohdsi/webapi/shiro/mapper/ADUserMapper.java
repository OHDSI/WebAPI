package org.ohdsi.webapi.shiro.mapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ADUserMapper extends UserMapper {
    @Value("${security.ad.userMapping.firstnameAttr}")
    private String firstnameKey;

    @Value("${security.ad.userMapping.middlenameAttr}")
    private String middlenameKey;

    @Value("${security.ad.userMapping.lastnameAttr}")
    private String lastnameKey;

    @Value("${security.ad.userMapping.usernameAttr}")
    private String usernameKey;

    @Value("${security.ad.userMapping.displaynameAttr}")
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