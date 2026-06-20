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
 * Verifies the opt-in anonymous-access mode
 * ({@code security.anonymousAccess.enabled=true}): token-less requests are
 * served as the built-in anonymous user instead of being rejected with 401,
 * while per-endpoint {@code @PreAuthorize} still governs what anonymous may do.
 *
 * <p>The flag only changes the web-layer catch-all (permitAll vs. authenticated);
 * method security is untouched. {@link SecurityIT} proves the same endpoints
 * return 401 under the default ({@code false}).
 *
 * <p>{@link WebApiIT} grants the anonymous user the admin role for tests. This
 * class removes that grant in {@link #demoteAnonymous()} so it can assert an
 * <em>unprivileged</em> anonymous user is reachable but still denied at gated
 * endpoints — i.e. authorization is evaluated, not bypassed.
 */
@TestPropertySource(properties = {
    "security.anonymousAccess.enabled=true",
    "security.defaultGlobalReadPermissions=true"
})
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
    public void anonymousReachesUngatedEndpointsWhenEnabled() {
        // /cohortdefinition (list) has no @PreAuthorize. Under default-deny it is
        // 401 (see SecurityIT); with the flag on the unprivileged anonymous user
        // reaches the handler instead of being rejected at the authentication layer.
        assertEquals(HttpStatus.OK, statusOf("/cohortdefinition"));
    }

    @Test
    public void preAuthorizeStillDeniesUnprivilegedAnonymous() {
        // /cache/clear is gated by isPermitted('admin:cache'). The demoted
        // anonymous user lacks it, so method security denies the request with 403 —
        // proving the flag opens reachability but does NOT bypass authorization.
        assertEquals(HttpStatus.FORBIDDEN, statusOf("/cache/clear"));
    }
}
