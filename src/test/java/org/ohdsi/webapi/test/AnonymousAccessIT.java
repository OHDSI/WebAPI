package org.ohdsi.webapi.test;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.security.authz.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

/**
 * Verifies the anonymous-principal model: token-less requests are served as the
 * built-in anonymous user (never rejected with 401 by the filter chain), and
 * per-endpoint {@code @PreAuthorize} alone governs what the anonymous principal
 * may do. Open listing endpoints return 200; gated endpoints return 403.
 *
 * <p>{@link WebApiIT} grants the anonymous user the admin role for tests. This
 * class removes that grant in {@link #demoteAnonymous()} so it can assert an
 * <em>unprivileged</em> anonymous user lists open endpoints but is still denied
 * at gated ones — i.e. authorization is evaluated, not bypassed.
 */
@TestPropertySource(properties = "security.defaultGlobalReadPermissions=true")
public class AnonymousAccessIT extends WebApiIT {

    /** A template with NO Authorization interceptor — simulates an anonymous caller. */
    private final TestRestTemplate anonymous = new TestRestTemplate();

    @Value("${datasource.ohdsi.schema:public}")
    private String schema;

    @Autowired
    private AuthorizationService authorizationService;

    /**
     * The superclass @Before grants the anonymous user (id -1) the admin role.
     * Remove it (and flush the authorization cache) so this class exercises a
     * genuinely unprivileged anonymous user.
     */
    @Before
    public void demoteAnonymous() {
        jdbcTemplate.execute(
            "DELETE FROM " + schema + ".sec_user_role WHERE user_id = -1 AND role_id = 2");
        authorizationService.clearCache();
    }

    /** Restore shared state for any test class that runs afterwards. */
    @After
    public void restoreAnonymous() {
        jdbcTemplate.execute(
            "INSERT INTO " + schema + ".sec_user_role (id, user_id, role_id, origin) " +
            "SELECT nextval('" + schema + ".sec_user_role_sequence'), -1, 2, 'SYSTEM' " +
            "WHERE NOT EXISTS (SELECT 1 FROM " + schema + ".sec_user_role WHERE user_id = -1 AND role_id = 2)");
        authorizationService.clearCache();
    }

    private HttpStatus statusOf(String path) {
        ResponseEntity<String> resp =
            anonymous.getForEntity(getBaseUri() + path, String.class);
        return HttpStatus.valueOf(resp.getStatusCode().value());
    }

    @Test
    public void anonymousMayListOpenEndpoints() {
        // Listing is open: a token-less request is served as the anonymous user and the
        // list endpoint returns 200 (its contents are filtered per-entity). Anonymous
        // needs neither a login nor a read grant to list.
        assertEquals(HttpStatus.OK, statusOf("/cohortdefinition"));
    }

    @Test
    public void preAuthorizeStillDeniesUnprivilegedAnonymous() {
        // Gated endpoints still reach method security and deny the unprivileged anonymous
        // user (403), proving authorization is evaluated, not bypassed:
        //   - /cache/clear is gated by admin:cache
        //   - /role/1 (a role definition) is gated by admin:security
        //   - /user (the user registry) is gated by the list permission
        assertEquals(HttpStatus.FORBIDDEN, statusOf("/cache/clear"));
        assertEquals(HttpStatus.FORBIDDEN, statusOf("/role/1"));
        assertEquals(HttpStatus.FORBIDDEN, statusOf("/user"));
    }
}
