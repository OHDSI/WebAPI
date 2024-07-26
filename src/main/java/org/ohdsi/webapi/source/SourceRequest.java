package org.ohdsi.webapi.source;

import java.util.Collection;
import javax.validation.constraints.NotNull;

public class SourceRequest {
    @NotNull
    private String name;
    @NotNull
    private String dialect;
    @NotNull
    private String key;
    @NotNull
    private String connectionString;

    private String username;
    private String password;
    private String krbAuthMethod;
    private String krbAdminServer;
    private Collection<SourceDaimon> daimons;
    private String keyfileName;
    private Boolean checkConnection;

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getDialect() {

        return dialect;
    }

    public void setDialect(String dialect) {

        this.dialect = dialect;
    }

    public String getKey() {

        return key;
    }

    public void setKey(String key) {

        this.key = key;
    }

    public String getConnectionString() {

        return connectionString;
    }

    public void setConnectionString(String connectionString) {

        this.connectionString = connectionString;
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

    public Collection<SourceDaimon> getDaimons() {

        return daimons;
    }

    public void setDaimons(Collection<SourceDaimon> daimons) {

        this.daimons = daimons;
    }

    public String getKrbAuthMethod() {
        return krbAuthMethod;
    }

    public void setKrbAuthMethod(String krbAuthMethod) {
        this.krbAuthMethod = krbAuthMethod;
    }

    public String getKrbAdminServer() {
        return krbAdminServer;
    }

    public void setKrbAdminServer(String krbAdminServer) {
        this.krbAdminServer = krbAdminServer;
    }

    public String getKeyfileName() {

        return keyfileName;
    }

    public void setKeyfileName(String keyfileName) {

        this.keyfileName = keyfileName;
    }

    public Boolean isCheckConnection() {
        return checkConnection;
    }

    public void setCheckConnection(Boolean checkConnection) {
        this.checkConnection = checkConnection;
    }
}
