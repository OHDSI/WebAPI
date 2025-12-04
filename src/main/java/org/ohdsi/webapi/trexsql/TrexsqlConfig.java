package org.ohdsi.webapi.trexsql;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for trexsql integration.
 * Maps to trexsql.* in application properties.
 */
@Configuration
@ConfigurationProperties(prefix = "trexsql")
public class TrexsqlConfig {

    private boolean enabled = false;
    private String cachePath = "./data/cache";
    private String extensionsPath;
    private Map<String, TrexsqlSourceConfig> sources = new HashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getCachePath() {
        return cachePath;
    }

    public void setCachePath(String cachePath) {
        this.cachePath = cachePath;
    }

    public String getExtensionsPath() {
        return extensionsPath;
    }

    public void setExtensionsPath(String extensionsPath) {
        this.extensionsPath = extensionsPath;
    }

    public Map<String, TrexsqlSourceConfig> getSources() {
        return sources;
    }

    public void setSources(Map<String, TrexsqlSourceConfig> sources) {
        this.sources = sources;
    }

    public TrexsqlSourceConfig getSourceConfig(String sourceKey) {
        return sources.get(sourceKey);
    }

    public boolean isEnabledForSource(String sourceKey) {
        if (!enabled) {
            return false;
        }
        TrexsqlSourceConfig sourceConfig = sources.get(sourceKey);
        return sourceConfig != null && sourceConfig.isEnabled();
    }
}
