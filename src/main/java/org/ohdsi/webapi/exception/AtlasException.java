package org.ohdsi.webapi.exception;

public class AtlasException extends RuntimeException {

    public AtlasException(Throwable ex){
        super(ex);
    }
}
