package org.ohdsi.webapi.mvc;

import org.junit.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
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
}
