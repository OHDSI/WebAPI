package org.ohdsi.webapi.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.security.authc.JwtService;
import org.ohdsi.webapi.security.authz.AuthorizationService;
import org.ohdsi.webapi.security.session.SessionService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Test harness that issues HTTP requests as a NON-admin user with no
 * data-source grant, so downstream tests can assert that source-scoped
 * endpoints deny them with 403.
 *
 * <p>The two smoke tests here validate the harness against
 * {@code PersonService}, which already enforces
 * {@code @PreAuthorize("hasSourceAccess(#sourceKey, READ)")}. This means
 * both tests pass without depending on any later security task.
 *
 * <p>Later tasks can extend this class (or replicate {@link #getAsLimitedUser})
 * to reuse the limited-user JWT.
 */
public class SourceAccessIT extends WebApiIT {

    // ---------------------------------------------------------------------------
    // Spring beans
    // ---------------------------------------------------------------------------

    @Autowired
    private JwtService jwtService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private AuthorizationService authorizationService;

    // ---------------------------------------------------------------------------
    // Limited-user state (set up in @Before)
    // ---------------------------------------------------------------------------

    /**
     * Hard-coded negative user-id so we never collide with the auto-sequence
     * (which starts at 1000).  Anonymous user sits at -1; we use -2 here.
     */
    private static final long LIMITED_USER_ID  = -2L;
    private static final long LIMITED_ROLE_ID  = -100L;   // personal role, non-admin
    private static final String LIMITED_LOGIN  = "limited";

    /** Bearer JWT for the limited user, minted fresh in each @Before. */
    private String limitedUserJwt;

    // ---------------------------------------------------------------------------
    // Test fixture
    // ---------------------------------------------------------------------------

    /**
     * Seeds:
     * <ol>
     *   <li>A test CDM source (Embedded_PG) in {@code source} table.</li>
     *   <li>A {@code sec_source} row granting the admin role (id = 2) READ
     *       access to that source — so the admin HTTP client is NOT denied.</li>
     *   <li>A limited user ({@code login='limited'}, id = -2) with only a
     *       personal non-admin role and NO {@code sec_source} grant — so the
     *       limited user IS denied with 403.</li>
     *   <li>A valid JWT + session for the limited user.</li>
     * </ol>
     *
     * The authorization cache is cleared after each seeding so that stale
     * entries from previous test runs in the same JVM do not interfere.
     */
    @Before
    public void setUpSourceAccessHarness() throws SQLException {
        String schema = getOhdsiSchema();

        // ---- 1. Seed CDM source --------------------------------------------------
        truncateTable(schema + ".source");
        resetSequence(schema + ".source_sequence");
        Source source = sourceRepository.saveAndFlush(getCdmSource());
        long sourceId = source.getId().longValue();

        // ---- 2. Grant admin role READ access to the source -----------------------
        //   role_id=2 is the built-in "admin" role (see baseline migration).
        //   hasSourceAccess() queries the sec_source table directly; the "*"
        //   wildcard permission in sec_permission does NOT bypass this check,
        //   so an explicit row is required.
        jdbcTemplate.execute(
            "INSERT INTO " + schema + ".sec_source (role_id, source_id, access_type) " +
            "VALUES (2, " + sourceId + ", 'READ') " +
            "ON CONFLICT DO NOTHING"
        );

        // ---- 3. Seed limited user (id=-2, login='limited') ----------------------
        //   Uses hard-coded negative IDs so sequences are never consumed.
        //   The sec_user table has a UNIQUE constraint on login; use ON CONFLICT
        //   to make the @Before idempotent across repeated test runs.
        jdbcTemplate.execute(
            "INSERT INTO " + schema + ".sec_user (id, login, name, origin) " +
            "VALUES (" + LIMITED_USER_ID + ", '" + LIMITED_LOGIN + "', 'Limited User', 'SYSTEM') " +
            "ON CONFLICT (login) DO NOTHING"
        );

        // ---- 4. Seed a personal (non-admin) role for the limited user -----------
        //   sec_role has a UNIQUE constraint on (name, system_role).
        jdbcTemplate.execute(
            "INSERT INTO " + schema + ".sec_role (id, name, system_role) " +
            "VALUES (" + LIMITED_ROLE_ID + ", '" + LIMITED_LOGIN + "', false) " +
            "ON CONFLICT DO NOTHING"
        );

        // ---- 5. Link the limited user to their personal role --------------------
        jdbcTemplate.execute(
            "INSERT INTO " + schema + ".sec_user_role (id, user_id, role_id, origin) " +
            "SELECT nextval('" + schema + ".sec_user_role_sequence'), " + LIMITED_USER_ID + ", " + LIMITED_ROLE_ID + ", 'SYSTEM' " +
            "WHERE NOT EXISTS (" +
            "  SELECT 1 FROM " + schema + ".sec_user_role " +
            "  WHERE user_id = " + LIMITED_USER_ID + " AND role_id = " + LIMITED_ROLE_ID +
            ")"
        );
        // No sec_source row for LIMITED_ROLE_ID → the limited user has NO source access.

        // ---- 6. Clear authorization cache so new rows are visible ---------------
        authorizationService.clearCache();

        // ---- 7. Mint a JWT + session for the limited user -----------------------
        UUID sessionId = sessionService.createSession(LIMITED_LOGIN);
        Date expiresAt = Date.from(Instant.now().plusSeconds(3600));
        limitedUserJwt = jwtService.generateToken(LIMITED_LOGIN, sessionId.toString(), expiresAt);
    }

    // ---------------------------------------------------------------------------
    // Reusable helper (the contract promised by the task brief)
    // ---------------------------------------------------------------------------

    /**
     * Issues a GET request to {@code getBaseUri() + path} as the limited user
     * (authenticated but with no source grant).
     *
     * <p>Reusable by subclasses or other tests that want to assert 403 on
     * source-scoped endpoints.
     *
     * @param path the path relative to the WebAPI base URI, e.g.
     *             {@code "/" + SOURCE_KEY + "/person/1"}
     * @return the raw HTTP response
     */
    protected ResponseEntity<String> getAsLimitedUser(String path) {
        TestRestTemplate limitedClient = new TestRestTemplate();
        limitedClient.getRestTemplate().getInterceptors().add((request, body, execution) -> {
            request.getHeaders().set("Authorization", "Bearer " + limitedUserJwt);
            return execution.execute(request, body);
        });
        return limitedClient.getForEntity(getBaseUri() + path, String.class);
    }

    // ---------------------------------------------------------------------------
    // Validating tests
    // ---------------------------------------------------------------------------

    /**
     * A limited user (authenticated, but with no source grant) must receive
     * HTTP 403 Forbidden when accessing a source-scoped endpoint.
     *
     * <p>PersonService already enforces
     * {@code @PreAuthorize("hasSourceAccess(#sourceKey, READ)")}
     * so this test passes without depending on any later task.
     */
    @Test
    public void limitedUserDeniedSourceScopedPerson() {
        ResponseEntity<String> r = getAsLimitedUser("/" + SOURCE_KEY + "/person/1");
        assertEquals(
            "Limited user (no source grant) should be denied with 403 Forbidden",
            HttpStatus.FORBIDDEN,
            HttpStatus.valueOf(r.getStatusCode().value())
        );
    }

    /**
     * The base-class admin user (anonymous, role=2, with a {@code sec_source}
     * READ grant seeded in {@link #setUpSourceAccessHarness()}) must NOT be
     * denied with 403 when accessing the same source-scoped endpoint.
     *
     * <p>The person id=1 likely does not exist in the embedded CDM, so we
     * expect 404 or 500 — but NOT 403.
     */
    @Test
    public void adminUserNotDeniedSourceScopedPerson() {
        ResponseEntity<String> r = getRestTemplate()
            .getForEntity(getBaseUri() + "/" + SOURCE_KEY + "/person/1", String.class);
        assertNotEquals(
            "Admin user (with source grant) should NOT be denied with 403 Forbidden",
            HttpStatus.FORBIDDEN,
            HttpStatus.valueOf(r.getStatusCode().value())
        );
    }
}
