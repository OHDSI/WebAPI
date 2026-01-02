package org.ohdsi.webapi.trexsql;

/**
 * Per-source configuration for TrexSQL integration.
 * Maps to trexsql.sources.{sourceKey} in application properties.
 */
public class TrexSQLSourceConfig {

    private boolean enabled = false;
    private String databaseCode;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDatabaseCode() {
        return databaseCode;
    }

    public void setDatabaseCode(String databaseCode) {
        this.databaseCode = databaseCode;
    }
}
