package org.ohdsi.webapi.trexsql.exception;

/**
 * Exception thrown when trexsql is not available or not configured.
 * Maps to HTTP 503 Service Unavailable.
 */
public class TrexsqlNotAvailableException extends RuntimeException {

    private final String sourceKey;

    public TrexsqlNotAvailableException(String message) {
        super(message);
        this.sourceKey = null;
    }

    public TrexsqlNotAvailableException(String sourceKey, String message) {
        super(message);
        this.sourceKey = sourceKey;
    }

    public String getSourceKey() {
        return sourceKey;
    }
}
