package org.ohdsi.webapi.exception;

// New BadRequest class is used to avoid breaking changes with existing BadRequest class from javax.ws.rs
public class BadRequestAtlasException extends AtlasException {

    public BadRequestAtlasException(String message, Throwable cause) {

        super(message, cause);
    }

    public BadRequestAtlasException(String message) {

        super(message);
    }

    public BadRequestAtlasException(Throwable ex) {
        super(ex);
    }
}
