package org.ohdsi.webapi.exception;

public class SourceDuplicateKeyException extends UserException {

    public SourceDuplicateKeyException(String message, Throwable cause) {

        super(message, cause);
    }

    public SourceDuplicateKeyException(String message) {

        super(message);
    }

    public SourceDuplicateKeyException(Throwable ex) {
        super(ex);
    }
}
