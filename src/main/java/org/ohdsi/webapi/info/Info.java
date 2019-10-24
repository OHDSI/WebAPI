package org.ohdsi.webapi.info;

import java.util.Map;

public class Info {

    private final String version;
    private final BuildInfo buildInfo;
    private final Map<String, Map<String, Object>> configuration;

    public Info(String version, BuildInfo buildInfo, Map<String, Map<String, Object>> configuration) {

        this.version = version;
        this.buildInfo = buildInfo;
        this.configuration = configuration;
    }

    public String getVersion() {

        return version;
    }

    public BuildInfo getBuildInfo() {

        return buildInfo;
    }

    public Map<String, Map<String, Object>> getConfiguration() {

        return configuration;
    }
}
