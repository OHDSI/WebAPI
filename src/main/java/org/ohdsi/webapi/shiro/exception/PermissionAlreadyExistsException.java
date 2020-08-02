package org.ohdsi.webapi.shiro.exception;

public class PermissionAlreadyExistsException extends Exception {
    public PermissionAlreadyExistsException() {
        super();
    }

    public PermissionAlreadyExistsException(String message) {
        super(message);
    }
}
