package org.ohdsi.webapi.shiny;

import org.ohdsi.webapi.shiny.posit.PositConnectClientException;

public class ConflictPositConnectException extends PositConnectClientException {
    public ConflictPositConnectException(String message) {
        super(message);
    }

    public ConflictPositConnectException(String message, Throwable cause) {
        super(message, cause);
    }
}
