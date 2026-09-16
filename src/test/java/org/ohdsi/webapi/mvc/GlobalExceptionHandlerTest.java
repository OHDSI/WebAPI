package org.ohdsi.webapi.mvc;

import org.junit.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * The constraint-violation branch used to assume a two-level cause chain, a
 * non-null message on it, and a PostgreSQL "Detail: " section. Creating a tag
 * whose name collides with an existing tag or tag group hit that branch and
 * threw a NullPointerException out of the handler, so the client saw a 500
 * instead of the intended 409 (OHDSI/Atlas3#211).
 */
public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = newHandler();

    private static GlobalExceptionHandler newHandler() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ApplicationEventPublisher noopPublisher = event -> { };
        ReflectionTestUtils.setField(handler, "eventPublisher", noopPublisher);
        return handler;
    }

    @Test
    public void returnsConflictAndTheDetailSectionWhenPostgresSuppliesOne() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "could not execute statement",
                new RuntimeException("constraint violation",
                        new RuntimeException("ERROR: duplicate key value violates unique constraint "
                                + "\"tags_name_idx\"\n  Detail: Key (lower(name))=(my tag) already exists.")));

        ResponseEntity<GlobalExceptionHandler.ErrorMessage> response = handler.handleDataIntegrityViolation(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Key (lower(name))=(my tag) already exists.", response.getBody().message());
    }

    @Test
    public void returnsConflictWhenTheViolationHasNoCauseAtAll() {
        DataIntegrityViolationException ex =
                new DataIntegrityViolationException("could not execute statement");

        ResponseEntity<GlobalExceptionHandler.ErrorMessage> response = handler.handleDataIntegrityViolation(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("could not execute statement", response.getBody().message());
    }

    @Test
    public void returnsConflictWhenTheCauseChainIsOnlyOneLevelDeep() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "could not execute statement", new RuntimeException("unique constraint violated"));

        ResponseEntity<GlobalExceptionHandler.ErrorMessage> response = handler.handleDataIntegrityViolation(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("unique constraint violated", response.getBody().message());
    }

    @Test
    public void returnsConflictWhenTheCauseCarriesNoMessage() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "could not execute statement", new RuntimeException(new RuntimeException()));

        ResponseEntity<GlobalExceptionHandler.ErrorMessage> response = handler.handleDataIntegrityViolation(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody().message());
    }

    /**
     * Databases other than PostgreSQL do not emit a "Detail: " section. The old
     * code fed indexOf's -1 straight into substring and silently dropped the
     * first seven characters of the message.
     */
    @Test
    public void doesNotTruncateMessagesThatHaveNoDetailSection() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "could not execute statement",
                new RuntimeException(new RuntimeException(
                        "Violation of UNIQUE KEY constraint 'tags_name_idx'.")));

        ResponseEntity<GlobalExceptionHandler.ErrorMessage> response = handler.handleDataIntegrityViolation(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().message().startsWith("Violation of UNIQUE KEY"));
    }

    /**
     * The service layer raises IllegalArgumentException for a generation,
     * characterization, analysis or preset the caller named but that does not
     * exist. With no handler for it these reached the generic fallback, which
     * answers 500 and replaces the message with the class name, so the caller
     * learned neither what was rejected nor that the fault was theirs
     * (OHDSI/Atlas3#291).
     */
    @Test
    public void reportsARejectedArgumentAsBadRequestAndKeepsItsReason() {
        ResponseEntity<GlobalExceptionHandler.ErrorMessage> response =
                handler.handleIllegalArgument(new IllegalArgumentException("There is no generation with id = 42."));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("There is no generation with id = 42.", response.getBody().message());
    }

    @Test
    public void namesTheExceptionWhenARejectedArgumentCarriesNoReason() {
        ResponseEntity<GlobalExceptionHandler.ErrorMessage> response =
                handler.handleIllegalArgument(new IllegalArgumentException());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().message().contains("java.lang.IllegalArgumentException"));
    }

    @Test
    public void unwrapsARejectedArgumentThatArrivesThroughAProxy() {
        UndeclaredThrowableException wrapped = new UndeclaredThrowableException(
                new InvocationTargetException(new IllegalArgumentException("Preset analysis with id=7 does not exist")));

        ResponseEntity<GlobalExceptionHandler.ErrorMessage> response = handler.handleUndeclaredThrowable(wrapped);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Preset analysis with id=7 does not exist", response.getBody().message());
    }

    /**
     * The wrapper's own class name only says a proxy was involved. Naming the
     * exception the caller actually hit is the point of the message.
     */
    @Test
    public void namesTheUnderlyingExceptionRatherThanTheProxyWrapper() {
        UndeclaredThrowableException wrapped = new UndeclaredThrowableException(
                new InvocationTargetException(new IllegalStateException("boom")));

        ResponseEntity<GlobalExceptionHandler.ErrorMessage> response = handler.handleUndeclaredThrowable(wrapped);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().message().contains("java.lang.IllegalStateException"));
    }
}
