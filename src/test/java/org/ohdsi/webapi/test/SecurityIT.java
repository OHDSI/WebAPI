package org.ohdsi.webapi.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Verifies the default-deny security model: every endpoint except the login
 * allow-list must reject unauthenticated (token-less) requests with 401.
 */
public class SecurityIT extends WebApiIT {

    /** A template with NO Authorization interceptor — simulates an anonymous caller. */
    private final TestRestTemplate anonymous = new TestRestTemplate();

    private HttpStatus statusOf(String path) {
        ResponseEntity<String> resp =
            anonymous.getForEntity(getBaseUri() + path, String.class);
        return HttpStatus.valueOf(resp.getStatusCode().value());
    }

    @Test
    public void protectedEndpointsRejectAnonymous() {
        // Sensitive endpoints that previously leaked data without a login.
        assertEquals(HttpStatus.UNAUTHORIZED, statusOf("/user"));
        assertEquals(HttpStatus.UNAUTHORIZED, statusOf("/role"));
        assertEquals(HttpStatus.UNAUTHORIZED, statusOf("/permission"));
        assertEquals(HttpStatus.UNAUTHORIZED, statusOf("/cohortdefinition"));
        assertEquals(HttpStatus.UNAUTHORIZED, statusOf("/conceptset"));
        assertEquals(HttpStatus.UNAUTHORIZED, statusOf("/tag"));
        assertEquals(HttpStatus.UNAUTHORIZED, statusOf("/source/sources"));
    }

    @Test
    public void loginAllowListIsAnonymous() {
        // These must stay reachable before login.
        assertEquals(HttpStatus.OK, statusOf("/info"));
        assertEquals(HttpStatus.OK, statusOf("/auth/providers"));
    }
}
