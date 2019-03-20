package org.ohdsi.webapi.shiro.mapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ADUserMapper extends UserMapper {
    @Value("${security.ad.userMapping.firstname}")
    private String firstnameKey;

    @Value("${security.ad.userMapping.middlename}")
    private String middlenameKey;

    @Value("${security.ad.userMapping.lastname}")
    private String lastnameKey;

    @Value("${security.ad.userMapping.username}")
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