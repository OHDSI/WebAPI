package org.ohdsi.webapi.source;

public class SourceDetails extends SourceInfo {

    private String connectionString;
    private String keytabName;
    private String authType;
    private String username;
    private String password;
    private String krbAdminServer;

    public SourceDetails(Source s) {
        super(s);
        setConnectionString(s.getSourceConnection());
        setKeytabName(s.getKeytabName());
        setAuthType(s.getKrbAuthMethod().toString().toLowerCase());
        setUsername(s.getUsername());
        setPassword(s.getPassword());
        setKrbAdminServer(s.getKrbAdminServer());
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getKrbAdminServer() {
        return krbAdminServer;
    }

    public void setKrbAdminServer(String krbAdminServer) {
        this.krbAdminServer = krbAdminServer;
    }
}
