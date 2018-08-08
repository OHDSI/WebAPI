package org.ohdsi.webapi.source;

public class SourceDetails extends SourceInfo {

    private String connectionString;
    private String keytabName;
    private String authType;

    public SourceDetails(Source s) {
        super(s);
        setConnectionString(s.getSourceConnection());
        setKeytabName(s.getKeytabName());
        setAuthType(s.getKrbAuthMethod().toString().toLowerCase());
    }

    public String getConnectionString() {

        return connectionString;
    }

    public void setConnectionString(String connectionString) {

        this.connectionString = connectionString;
    }

    public String getKeytabName() {
        return keytabName;
    }

    public void setKeytabName(String keytabName) {
        this.keytabName = keytabName;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }
}
