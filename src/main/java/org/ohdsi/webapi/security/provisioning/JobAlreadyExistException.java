package org.ohdsi.webapi.security.provisioning;

public class JobAlreadyExistException extends RuntimeException {
  public JobAlreadyExistException() {
  }

  public JobAlreadyExistException(String message) {
    super(message);
  }
}
