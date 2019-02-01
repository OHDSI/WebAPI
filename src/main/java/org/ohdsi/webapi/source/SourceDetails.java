package org.ohdsi.webapi.source;

public class SourceDetails extends SourceInfo {

    private String connectionString;
    private String username;
    private String password;
    private String krbAuthMethod;
    private String keytabName;
    private String krbAdminServer;

    public SourceDetails(Source s) {
        super(s);
        setConnectionString(s.getSourceConnection());
        setKeytabName(s.getKeytabName());
        setKrbAuthMethod(s.getKrbAuthMethod().toString().toLowerCase());
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

    public String getKrbAuthMethod() {
        return krbAuthMethod;
    }

    public void setKrbAuthMethod(String krbAuthMethod) {
        this.krbAuthMethod = krbAuthMethod;
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
