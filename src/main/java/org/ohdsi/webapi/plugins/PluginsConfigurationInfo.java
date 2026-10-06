package org.ohdsi.webapi.plugins;

import org.ohdsi.info.ConfigurationInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;

@Component
public class PluginsConfigurationInfo extends ConfigurationInfo {
    private static final String KEY = "plugins";

    public PluginsConfigurationInfo(
            @Autowired(required = false) List<WebApiPlugin> plugins,
            @Value("${atlasgis.enabled}") Boolean atlasgisEnabled) {
        if (plugins == null) {
            plugins = Collections.emptyList();
        }
        for (WebApiPlugin plugin : plugins) {
            properties.put(plugin.getId() + "Enabled", plugin.isActive());
        }
        properties.put("atlasgisEnabled", atlasgisEnabled);
    }

    @Override
    public String getKey() {
        return KEY;
    }
}
