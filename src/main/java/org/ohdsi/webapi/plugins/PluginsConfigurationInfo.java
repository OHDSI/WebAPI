package org.ohdsi.webapi.plugins;

import org.ohdsi.info.ConfigurationInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PluginsConfigurationInfo extends ConfigurationInfo {
    private static final String KEY = "plugins";

    public PluginsConfigurationInfo(@Value("${atlasgis.enabled}") Boolean atlasgisEnabled) {
        properties.put("atlasgisEnabled", atlasgisEnabled);
    }

    @Override
    public String getKey() {
        return KEY;
    }
}
