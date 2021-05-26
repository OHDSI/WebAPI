package org.ohdsi.webapi.exception;

import org.springframework.core.convert.ConversionFailedException;

public class ConversionAtlasException extends ConversionFailedException {
    private String message;

    public ConversionAtlasException(String message) {
        super(null, null, null, null);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
