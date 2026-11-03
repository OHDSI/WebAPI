package org.ohdsi.webapi.plugins;

import org.ohdsi.info.ConfigurationInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class PluginsConfigurationInfo extends ConfigurationInfo {
    private static final String KEY = "plugins";

    private final List<WebApiPlugin> plugins;
    private final Boolean atlasgisEnabled;

    public PluginsConfigurationInfo(
            @Autowired(required = false) List<WebApiPlugin> plugins,
            @Value("${atlasgis.enabled}") Boolean atlasgisEnabled) {
        this.plugins = plugins == null ? Collections.emptyList() : plugins;
        this.atlasgisEnabled = atlasgisEnabled;
    }

    // A plugin can only report itself active once its engine has started, which
    // may happen after this bean is built, and it can stop again at runtime.
    // Ask on every read rather than freezing whatever was true at construction.
    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> current = new LinkedHashMap<>();
        for (WebApiPlugin plugin : plugins) {
            current.put(plugin.getId() + "Enabled", plugin.isActive());
        }
        current.put("atlasgisEnabled", atlasgisEnabled);
        return current;
    }

    @Override
    public String getKey() {
        return KEY;
    }
}
