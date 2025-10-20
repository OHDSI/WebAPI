package org.ohdsi.webapi.shiny.posit;

public class PositConnectClientException extends RuntimeException {
    public PositConnectClientException(String message) {
        super(message);
    }

    public PositConnectClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
