package org.ohdsi.webapi.arachne.datasource.util;

import org.ohdsi.webapi.arachne.datasource.dto.AuthMethod;
import org.ohdsi.webapi.arachne.datasource.dto.KerberosAuthMechanism;

public class ConnectionParams {
    private String dbms;
    private String server;
    private String user;
    private String password;
    private String port;
    private String schema;
    private String extraSettings;
    private String connectionString;
    private KerberosAuthMechanism krbAuthMechanism;
    private AuthMethod authMethod;
    private String krbFQDN;
    private String krbRealm;

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getDbms() {

        return dbms;
    }

    public void setDbms(String dbms) {

        this.dbms = dbms;
    }

    public String getServer() {

        return server;
    }

    public void setServer(String server) {

        this.server = server;
    }

    public String getUser() {

        return user;
    }

    public void setUser(String user) {

        this.user = user;
    }

    public String getPassword() {

        return password;
    }

    public void setPassword(String password) {

        this.password = password;
    }

    public String getPort() {

        return port;
    }

    public void setPort(String port) {

        this.port = port;
    }

    public String getSchema() {

        return schema;
    }

    public void setSchema(String schema) {

        this.schema = schema;
    }

    public String getExtraSettings() {

        return extraSettings;
    }

    public void setExtraSettings(String extraSettings) {

        this.extraSettings = extraSettings;
    }

    public KerberosAuthMechanism getKrbAuthMechanism() {

        return krbAuthMechanism;
    }

    public void setKrbAuthMechanism(KerberosAuthMechanism krbAuthMechanism) {

        this.krbAuthMechanism = krbAuthMechanism;
    }

    public String getKrbFQDN() {

        return krbFQDN;
    }

    public void setKrbFQDN(String krbFQDN) {

        this.krbFQDN = krbFQDN;
    }

    public String getKrbRealm() {

        return krbRealm;
    }

    public void setKrbRealm(String krbRealm) {

        this.krbRealm = krbRealm;
    }

    public AuthMethod getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(AuthMethod authMethod) {
        this.authMethod = authMethod;
    }
}
