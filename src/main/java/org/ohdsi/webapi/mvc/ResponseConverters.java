package org.ohdsi.webapi.mvc;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.http.ResponseEntity;

/**
 * Utility class to convert JAX-RS ResponseEntity objects to Spring ResponseEntity.
 * Used during migration to facilitate gradual conversion of endpoints.
 */
public class ResponseConverters {

    /**
     * Convert JAX-RS ResponseEntity to Spring ResponseEntity
     */
    public static <T> ResponseEntity<T> toResponseEntity(ResponseEntity response) {
        if (response == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        HttpStatus status = HttpStatus.valueOf(response.getStatusCode().value());

        @SuppressWarnings("unchecked")
        T body = (T) response.getBody();

        if (body == null) {
            return ResponseEntity.status(status).build();
        }

        return ResponseEntity.status(status).body(body);
    }

    // JAX-RS conversion methods removed - no longer needed after migration to Spring MVC

    /**
     * Create ResponseEntity from status code and entity
     */
    public static <T> ResponseEntity<T> fromStatusCode(int statusCode, T entity) {
        return ResponseEntity.status(statusCode).body(entity);
    }
}
