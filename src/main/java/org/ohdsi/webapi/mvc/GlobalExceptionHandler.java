package org.ohdsi.webapi.mvc;

import org.ohdsi.webapi.arachne.logging.event.FailedDbConnectEvent;
import org.ohdsi.webapi.exception.BadRequestAtlasException;
import org.ohdsi.webapi.exception.ConceptNotExistException;
import org.ohdsi.webapi.exception.ConversionAtlasException;
import org.ohdsi.webapi.exception.UserException;
import org.ohdsi.webapi.vocabulary.ConceptRecommendedNotInstalledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Objects;

/**
 * Global exception handler for REST controllers.
 * Handles all exceptions and returns appropriate HTTP responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String DETAIL = "Detail: ";

    /** Simple error body for REST responses (replaces spring-messaging ErrorMessage). */
    public record ErrorMessage(String message) {
        public ErrorMessage(Throwable t) {
            this(t.getMessage());
        }
    }

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * Handle database connection failures.
     */
    @ExceptionHandler(CannotGetJdbcConnectionException.class)
    public ResponseEntity<String> handleDatabaseConnectionException(CannotGetJdbcConnectionException exception) {
        eventPublisher.publishEvent(new FailedDbConnectEvent(this, exception.getMessage()));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
    }

    /**
     * Handle data integrity violations (e.g., unique constraint violations)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorMessage> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        logException(ex);

        RuntimeException sanitizedException = new RuntimeException(describeConstraintViolation(ex));
        sanitizedException.setStackTrace(new StackTraceElement[0]);

        ErrorMessage errorMessage = new ErrorMessage(sanitizedException);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
    }

    /**
     * Pull a human-readable reason out of a constraint violation.
     *
     * <p>The cause chain is not guaranteed to be two levels deep, nor to carry a
     * message, and only PostgreSQL appends a "Detail: " section. Assuming any of
     * that threw a NullPointerException (or silently truncated the message by
     * seven characters when {@code indexOf} returned -1) from inside the handler,
     * which Spring then reported as a 500 instead of the intended 409.
     */
    private static String describeConstraintViolation(DataIntegrityViolationException ex) {
        for (Throwable t = ex; t != null; t = t.getCause()) {
            String message = t.getMessage();
            if (message == null) {
                continue;
            }
            int detailIndex = message.indexOf(DETAIL);
            if (detailIndex >= 0) {
                return message.substring(detailIndex + DETAIL.length());
            }
        }
        String message = ex.getMostSpecificCause().getMessage();
        return message != null ? message : "The request conflicts with existing data.";
    }

    /**
     * Handle authorization/permission exceptions
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorMessage> handleAuthorizationException(Exception ex) {
        logException(ex);
        ex.setStackTrace(new StackTraceElement[0]);
        ErrorMessage errorMessage = new ErrorMessage(ex);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorMessage);
    }

    /**
     * Handle ResponseStatusException.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorMessage> handleResponseStatusException(ResponseStatusException ex) {
        logException(ex);
        RuntimeException sanitizedException = new RuntimeException(ex.getReason() != null ? ex.getReason() : ex.getMessage());
        sanitizedException.setStackTrace(new StackTraceElement[0]);
        ErrorMessage errorMessage = new ErrorMessage(sanitizedException);
        return ResponseEntity.status(ex.getStatusCode()).body(errorMessage);
    }

    /**
     * Handle Spring MVC resource not found exceptions
     * (e.g., when no controller mapping exists for a URL)
     */
    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<ErrorMessage> handleResourceNotFoundException(Exception ex) {
        logException(ex);
        RuntimeException sanitizedException = new RuntimeException("Resource not found");
        sanitizedException.setStackTrace(new StackTraceElement[0]);
        ErrorMessage errorMessage = new ErrorMessage(sanitizedException);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
    }

    /**
     * Handle bad request exceptions
     */
    @ExceptionHandler({ConceptNotExistException.class, BadRequestAtlasException.class})
    public ResponseEntity<ErrorMessage> handleBadRequestException(Exception ex) {
        logException(ex);
        ex.setStackTrace(new StackTraceElement[0]);
        ErrorMessage errorMessage = new ErrorMessage(ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }

    /**
     * Handle concept not installed exceptions
     */
    @ExceptionHandler(ConceptRecommendedNotInstalledException.class)
    public ResponseEntity<ErrorMessage> handleConceptNotInstalled(ConceptRecommendedNotInstalledException ex) {
        logException(ex);
        ex.setStackTrace(new StackTraceElement[0]);
        ErrorMessage errorMessage = new ErrorMessage(ex);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(errorMessage);
    }

    /**
     * Handle undeclared throwable exceptions (proxied exceptions)
     */
    @ExceptionHandler(UndeclaredThrowableException.class)
    public ResponseEntity<ErrorMessage> handleUndeclaredThrowable(UndeclaredThrowableException ex) {
        logException(ex);

        Throwable throwable = getThrowable(ex);
        HttpStatus status;
        Throwable responseException;

        if (Objects.nonNull(throwable)) {
            if (throwable instanceof AccessDeniedException) {
                status = HttpStatus.FORBIDDEN;
                responseException = throwable;
            } else if (throwable instanceof BadRequestAtlasException || throwable instanceof ConceptNotExistException) {
                status = HttpStatus.BAD_REQUEST;
                responseException = throwable;
            } else if (throwable instanceof ConversionAtlasException) {
                status = HttpStatus.BAD_REQUEST;
                // New exception must be created or direct self-reference exception will be thrown
                responseException = new RuntimeException(throwable.getMessage());
            } else {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                responseException = new RuntimeException("An exception occurred: " + ex.getClass().getName());
            }
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            responseException = new RuntimeException("An exception occurred: " + ex.getClass().getName());
        }

        responseException.setStackTrace(new StackTraceElement[0]);
        ErrorMessage errorMessage = new ErrorMessage(responseException);
        return ResponseEntity.status(status).body(errorMessage);
    }

    /**
     * Handle user exceptions
     */
    @ExceptionHandler(UserException.class)
    public ResponseEntity<ErrorMessage> handleUserException(UserException ex) {
        logException(ex);
        // Create new message to prevent sending error information to client
        RuntimeException sanitizedException = new RuntimeException(ex.getMessage());
        sanitizedException.setStackTrace(new StackTraceElement[0]);
        ErrorMessage errorMessage = new ErrorMessage(sanitizedException);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
    }

    /**
     * Handle all other exceptions (fallback)
     * Replaces: GenericExceptionMapper default case
     */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorMessage> handleGenericException(Throwable ex) {
        logException(ex);
        // Create new message to prevent sending error information to client
        RuntimeException sanitizedException = new RuntimeException("An exception occurred: " + ex.getClass().getName());
        sanitizedException.setStackTrace(new StackTraceElement[0]);
        ErrorMessage errorMessage = new ErrorMessage(sanitizedException);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
    }

    /**
     * Extract the target exception from UndeclaredThrowableException
     */
    private Throwable getThrowable(UndeclaredThrowableException ex) {
        if (Objects.nonNull(ex.getUndeclaredThrowable()) && ex.getUndeclaredThrowable() instanceof InvocationTargetException) {
            InvocationTargetException ite = (InvocationTargetException) ex.getUndeclaredThrowable();
            return ite.getTargetException();
        }
        return null;
    }

    /**
     * Log exception with stack trace (formatted by Logback)
     */
    private void logException(Throwable ex) {
        LOGGER.error(ex.toString(), ex);
    }
}
