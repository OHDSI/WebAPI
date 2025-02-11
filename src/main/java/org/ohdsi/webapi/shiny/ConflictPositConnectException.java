package org.ohdsi.webapi.shiny;

public class ConflictPositConnectException extends PositConnectClientException {
    public ConflictPositConnectException(String message) {
        super(message);
    }

    public ConflictPositConnectException(String message, Throwable cause) {
        super(message, cause);
    }
}
