package org.ohdsi.webapi.security;

import org.ohdsi.info.ConfigurationInfo;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.shiro.management.AtlasRegularSecurity;
import org.ohdsi.webapi.shiro.management.Security;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class SecurityConfigurationInfo extends ConfigurationInfo {

    private static final String KEY = "security";

    public SecurityConfigurationInfo(@Value("${security.provider}") String securityProvider,
                                     @Value("${security.saml.enabled}") Boolean samlEnabled,
                                     Security atlasSecurity) {

        boolean enabled = !Objects.equals(securityProvider, Constants.SecurityProviders.DISABLED);

        properties.put("enabled", enabled);
        properties.put("samlEnabled", samlEnabled);
        boolean samlActivated = false;
        if (atlasSecurity instanceof AtlasRegularSecurity) {
            samlActivated = ((AtlasRegularSecurity) atlasSecurity).isSamlEnabled();
        }
        properties.put("samlActivated", samlActivated);
    }

    @Override
    public String getKey() {

        return KEY;
    }
}
