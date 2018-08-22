package org.ohdsi.webapi.user.importer.model;

public class ConnectionInfo {

  private ConnectionState state;
  private String message;
  private String details;

  public ConnectionState getState() {
    return state;
  }

  public void setState(ConnectionState state) {
    this.state = state;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getDetails() {
    return details;
  }

  public void setDetails(String details) {
    this.details = details;
  }

  public enum ConnectionState {
    SUCCESS, FAILED
  }
}
