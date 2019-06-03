package org.ohdsi.webapi.exception;

public class AtlasException extends RuntimeException {

    public AtlasException(Exception ex){
        super(ex);
    }
}
