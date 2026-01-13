package org.ohdsi.webapi.trexsql;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Global trexsql configuration. Per-source config is in the source table (is_cache_enabled).
 */
@ConfigurationProperties(prefix = "trexsql")
public class TrexSQLConfig {

    private boolean enabled = false;
    private String cachePath = "./data/cache";
    private String extensionsPath;

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
}
