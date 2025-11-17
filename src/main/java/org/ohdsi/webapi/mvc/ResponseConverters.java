package org.ohdsi.webapi.mvc;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.ws.rs.core.Response;

/**
 * Utility class to convert JAX-RS Response objects to Spring ResponseEntity.
 * Used during migration to facilitate gradual conversion of endpoints.
 */
public class ResponseConverters {

    /**
     * Convert JAX-RS Response to Spring ResponseEntity
     */
    public static <T> ResponseEntity<T> toResponseEntity(Response response) {
        if (response == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        HttpStatus status = HttpStatus.valueOf(response.getStatus());

        @SuppressWarnings("unchecked")
        T body = (T) response.getEntity();

        if (body == null) {
            return ResponseEntity.status(status).build();
        }

        return ResponseEntity.status(status).body(body);
    }

    /**
     * Convert JAX-RS Response.Status to Spring HttpStatus
     */
    public static HttpStatus toHttpStatus(Response.Status status) {
        return HttpStatus.valueOf(status.getStatusCode());
    }

    /**
     * Convert JAX-RS Response.StatusType to Spring HttpStatus
     */
    public static HttpStatus toHttpStatus(Response.StatusType statusType) {
        return HttpStatus.valueOf(statusType.getStatusCode());
    }

    /**
     * Create ResponseEntity from JAX-RS status and entity
     */
    public static <T> ResponseEntity<T> fromJaxRs(Response.Status status, T entity) {
        return ResponseEntity.status(toHttpStatus(status)).body(entity);
    }

    /**
     * Create ResponseEntity from status code and entity
     */
    public static <T> ResponseEntity<T> fromStatusCode(int statusCode, T entity) {
        return ResponseEntity.status(statusCode).body(entity);
    }
}
