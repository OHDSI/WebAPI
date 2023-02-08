package org.ohdsi.webapi.info;

import java.util.HashMap;
import java.util.Map;

public abstract class ConfigurationInfo {

    protected final Map<String, Object> properties = new HashMap<>();

    public abstract String getKey();

    public Map<String, Object> getProperties() {

        return properties;
    }
}
