package org.ohdsi.webapi.exception;

public class AtlasException extends RuntimeException {
    public AtlasException() {
    }

    public AtlasException(String message, Throwable cause) {
        super(message, cause);
    }

    public AtlasException(String message) {
        super(message);
    }

    public AtlasException(Throwable ex) {
        super(ex);
    }
}
