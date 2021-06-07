package org.ohdsi.webapi.exception;

public class ConceptNotExistException extends AtlasException{
    public ConceptNotExistException(String message) {
        super(message);
    }

    public ConceptNotExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
