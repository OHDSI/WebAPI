package org.ohdsi.webapi.trexsql.exception;

/**
 * Exception thrown when a cache file is not found for a source.
 * Maps to HTTP 404 Not Found.
 */
public class CacheNotFoundException extends RuntimeException {

    private final String sourceKey;

    public CacheNotFoundException(String sourceKey) {
        super("No cache exists for source: " + sourceKey);
        this.sourceKey = sourceKey;
    }

    public CacheNotFoundException(String sourceKey, String message) {
        super(message);
        this.sourceKey = sourceKey;
    }

    public String getSourceKey() {
        return sourceKey;
    }
}
