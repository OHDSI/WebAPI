package org.ohdsi.webapi.mvc;

import org.apache.shiro.authz.UnauthorizedException;
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
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Objects;

/**
 * Spring MVC Global Exception Handler
 *
 * Replaces Jersey JAX-RS exception mappers:
 * - GenericExceptionMapper.java (handles all throwables)
 * - JdbcExceptionMapper.java (handles database connection failures)
 *
 * Migration Status: Replaces both JAX-RS @Provider exception mappers
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String DETAIL = "Detail: ";

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * Handle database connection failures
     * Replaces: JdbcExceptionMapper
     */
    @ExceptionHandler(CannotGetJdbcConnectionException.class)
    public ResponseEntity<Void> handleDatabaseConnectionException(CannotGetJdbcConnectionException exception) {
        eventPublisher.publishEvent(new FailedDbConnectEvent(this, exception.getMessage()));
        return ResponseEntity.ok().build();
    }

    /**
     * Handle data integrity violations (e.g., unique constraint violations)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorMessage> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        logException(ex);

        String cause = ex.getCause().getCause().getMessage();
        cause = cause.substring(cause.indexOf(DETAIL) + DETAIL.length());
        RuntimeException sanitizedException = new RuntimeException(cause);
        sanitizedException.setStackTrace(new StackTraceElement[0]);

        ErrorMessage errorMessage = new ErrorMessage(sanitizedException);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
    }

    /**
     * Handle authorization/permission exceptions
     */
    @ExceptionHandler({UnauthorizedException.class, ForbiddenException.class})
    public ResponseEntity<ErrorMessage> handleAuthorizationException(Exception ex) {
        logException(ex);
        ex.setStackTrace(new StackTraceElement[0]);
        ErrorMessage errorMessage = new ErrorMessage(ex);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorMessage);
    }

    /**
     * Handle not found exceptions
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorMessage> handleNotFoundException(NotFoundException ex) {
        logException(ex);
        ex.setStackTrace(new StackTraceElement[0]);
        ErrorMessage errorMessage = new ErrorMessage(ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
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
    @ExceptionHandler({BadRequestException.class, ConceptNotExistException.class})
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
            if (throwable instanceof UnauthorizedException || throwable instanceof ForbiddenException) {
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
     * Log exception with full stack trace
     */
    private void logException(Throwable ex) {
        StringWriter errorStackTrace = new StringWriter();
        ex.printStackTrace(new PrintWriter(errorStackTrace));
        LOGGER.error(errorStackTrace.toString());
    }
}
