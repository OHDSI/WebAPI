package org.ohdsi.webapi.exception;

public class ConceptNotExistException extends RuntimeException {
    public ConceptNotExistException(String message) {
        super(message);
    }
}
