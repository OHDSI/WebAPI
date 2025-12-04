package org.ohdsi.webapi.trexsql.exception;

/**
 * Exception thrown when cache creation is already in progress for a source.
 * Maps to HTTP 409 Conflict.
 */
public class CacheCreationInProgressException extends RuntimeException {

    private final String sourceKey;

    public CacheCreationInProgressException(String sourceKey) {
        super("Cache creation is already in progress for source: " + sourceKey);
        this.sourceKey = sourceKey;
    }

    public String getSourceKey() {
        return sourceKey;
    }
}
