package org.ohdsi.webapi.security;

import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.info.ConfigurationInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class SecurityConfigurationInfo extends ConfigurationInfo {

    private static final String KEY = "security";

    public SecurityConfigurationInfo(@Value("${security.provider}") String securityProvider) {

        boolean enabled = !Objects.equals(securityProvider, Constants.SecurityProviders.DISABLED);

        properties.put("enabled", enabled);
    }

    @Override
    public String getKey() {

        return KEY;
    }
}
