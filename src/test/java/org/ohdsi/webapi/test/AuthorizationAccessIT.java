package org.ohdsi.webapi.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.security.authc.JwtService;
import org.ohdsi.webapi.security.authz.AuthorizationService;
import org.ohdsi.webapi.security.session.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Positive-path authorization: a user who holds the matching permission must be
 * ALLOWED (not 403). Complements {@link SourceAccessIT} / {@link SecurityIT},
 * which only prove that access is denied — they cannot catch an over-strict
 * {@code @PreAuthorize} that wrongly denies a legitimate user.
 *
 * <p>The "reader" user is granted the generic {@code read} permission, which
 * wildcard-implies every {@code read:<domain>}; so it must reach the domain
 * read/list endpoints, but must still be denied admin operations.
 */
public class AuthorizationAccessIT extends WebApiIT {

    @Autowired
    private JwtService jwtService;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private AuthorizationService authorizationService;

    private static final long READER_USER_ID = -3L;
    private static final long READER_ROLE_ID = -101L;
    private static final String READER_LOGIN = "reader";

    private String readerJwt;

    @Before
    public void setUpReader() {
        String schema = getOhdsiSchema();

        jdbcTemplate.execute(
            "INSERT INTO " + schema + ".sec_user (id, login, name, origin) " +
            "VALUES (" + READER_USER_ID + ", '" + READER_LOGIN + "', 'Reader User', 'SYSTEM') " +
            "ON CONFLICT (login) DO NOTHING");
        jdbcTemplate.execute(
            "INSERT INTO " + schema + ".sec_role (id, name, system_role) " +
            "VALUES (" + READER_ROLE_ID + ", '" + READER_LOGIN + "', false) " +
            "ON CONFLICT DO NOTHING");
        jdbcTemplate.execute(
            "INSERT INTO " + schema + ".sec_user_role (id, user_id, role_id, origin) " +
            "SELECT nextval('" + schema + ".sec_user_role_sequence'), " + READER_USER_ID + ", " + READER_ROLE_ID + ", 'SYSTEM' " +
            "WHERE NOT EXISTS (SELECT 1 FROM " + schema + ".sec_user_role WHERE user_id = " + READER_USER_ID + " AND role_id = " + READER_ROLE_ID + ")");
        // Grant the reader's role 'read' (read any asset) and 'list' (list platform reference data) —
        // the permissions a normal authenticated user holds.
        jdbcTemplate.execute(
            "INSERT INTO " + schema + ".sec_role_permission (id, role_id, permission_id) " +
            "SELECT nextval('" + schema + ".sec_role_permission_sequence'), " + READER_ROLE_ID + ", p.id " +
            "FROM " + schema + ".sec_permission p WHERE p.value IN ('read', 'list') " +
            "AND NOT EXISTS (SELECT 1 FROM " + schema + ".sec_role_permission rp WHERE rp.role_id = " + READER_ROLE_ID + " AND rp.permission_id = p.id)");

        authorizationService.clearCache();

        UUID sessionId = sessionService.createSession(READER_LOGIN);
        Date expiresAt = Date.from(Instant.now().plusSeconds(3600));
        readerJwt = jwtService.generateToken(READER_LOGIN, sessionId.toString(), expiresAt);
    }

    private ResponseEntity<String> getAsReader(String path) {
        TestRestTemplate client = new TestRestTemplate();
        client.getRestTemplate().getInterceptors().add((request, body, execution) -> {
            request.getHeaders().set("Authorization", "Bearer " + readerJwt);
            return execution.execute(request, body);
        });
        return client.getForEntity(getBaseUri() + path, String.class);
    }

    private void assertReaderAllowed(String path) {
        ResponseEntity<String> r = getAsReader(path);
        int sc = r.getStatusCode().value();
        assertTrue(
            "Reader (granted 'read') must be allowed on " + path + " — got " + sc
                + " (a 401/403 here means the endpoint is over-restricted beyond a read permission)",
            sc >= 200 && sc < 300);
    }

    @Test
    public void readerAllowedCohortDefinitionList() {
        assertReaderAllowed("/cohortdefinition");
    }

    @Test
    public void readerAllowedConceptSetList() {
        assertReaderAllowed("/conceptset");
    }

    @Test
    public void readerAllowedIncidenceRateList() {
        // Also guards the IR list endpoint (interface-based controller) against
        // over-restriction beyond read:incidence.
        assertReaderAllowed("/ir");
    }

    @Test
    public void readerAllowedUserList() {
        // The user registry is gated by the 'list' permission (held by the reader and the built-in
        // public role), not admin:security, so the sharing workflow keeps working. Anonymous holds
        // no 'list' and is denied.
        assertReaderAllowed("/user");
    }

    @Test
    public void readerDeniedAdminRoleManagement() {
        // Listing roles is open under the anonymous-principal model, but a role *definition*
        // (GET /role/{id}) stays admin:security — 'read' must NOT grant it.
        ResponseEntity<String> r = getAsReader("/role/1");
        assertEquals(
            "Reader (granted only 'read') must be denied admin role management",
            HttpStatus.FORBIDDEN,
            HttpStatus.valueOf(r.getStatusCode().value()));
    }
}
