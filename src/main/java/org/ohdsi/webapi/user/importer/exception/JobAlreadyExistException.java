package org.ohdsi.webapi.user.importer.exception;

public class JobAlreadyExistException extends RuntimeException {
  public JobAlreadyExistException() {
  }

  public JobAlreadyExistException(String message) {
    super(message);
  }
}
