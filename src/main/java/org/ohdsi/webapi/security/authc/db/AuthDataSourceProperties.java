package org.ohdsi.webapi.security.authc.db;

public class AuthDataSourceProperties {

  private String driverClassName;
  private String url;
  private String username;
  private String password;
  private String schema;
  private String connectionTestQuery;
  private long connectionTestQueryTimeout;
  private int maximumPoolSize;
  private int minimumIdle;
  private int connectionTimeout;
  private boolean registerMbeans;
  private String poolName;

  public AuthDataSourceProperties() {
    // default constructor needed for Spring binding
  }

  public String getDriverClassName() {
    return driverClassName;
  }

  public void setDriverClassName(String driverClassName) {
    this.driverClassName = driverClassName;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
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

  public String getSchema() {
    return schema;
  }

  public void setSchema(String schema) {
    this.schema = schema;
  }

  public String getConnectionTestQuery() {
    return connectionTestQuery;
  }

  public void setConnectionTestQuery(String connectionTestQuery) {
    this.connectionTestQuery = connectionTestQuery;
  }

  public long getConnectionTestQueryTimeout() {
    return connectionTestQueryTimeout;
  }

  public void setConnectionTestQueryTimeout(long connectionTestQueryTimeout) {
    this.connectionTestQueryTimeout = connectionTestQueryTimeout;
  }

  public int getMaximumPoolSize() {
    return maximumPoolSize;
  }

  public void setMaximumPoolSize(int maximumPoolSize) {
    this.maximumPoolSize = maximumPoolSize;
  }

  public int getMinimumIdle() {
    return minimumIdle;
  }

  public void setMinimumIdle(int minimumIdle) {
    this.minimumIdle = minimumIdle;
  }

  public int getConnectionTimeout() {
    return connectionTimeout;
  }

  public void setConnectionTimeout(int connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
  }

  public boolean isRegisterMbeans() {
    return registerMbeans;
  }

  public void setRegisterMbeans(boolean registerMbeans) {
    this.registerMbeans = registerMbeans;
  }

  public String getPoolName() {
    return poolName;
  }

  public void setPoolName(String poolName) {
    this.poolName = poolName;
  }

  @Override
  public String toString() {
    return "AuthDataSourceProperties{" +
        "driverClassName='" + driverClassName + '\'' +
        ", url='" + url + '\'' +
        ", username='" + username + '\'' +
        ", schema='" + schema + '\'' +
        ", connectionTestQuery='" + connectionTestQuery + '\'' +
        ", connectionTestQueryTimeout=" + connectionTestQueryTimeout +
        ", maximumPoolSize=" + maximumPoolSize +
        ", minimumIdle=" + minimumIdle +
        ", connectionTimeout=" + connectionTimeout +
        ", registerMbeans=" + registerMbeans +
        ", poolName='" + poolName + '\'' +
        '}';
  }
}
