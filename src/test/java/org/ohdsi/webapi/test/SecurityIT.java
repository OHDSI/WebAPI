package org.ohdsi.webapi.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

/**
 * Verifies the anonymous-access toggle in its disabled state. With
 * {@code security.allowAnonymousAccess=false}, a token-less request is rejected with
 * 401 by the filter chain for every endpoint outside the bootstrap allow-list, while
 * the bootstrap endpoints (used by the login page) stay reachable. The enabled
 * (default) mode is exercised by {@link AnonymousAccessIT}.
 */
@TestPropertySource(properties = "security.allowAnonymousAccess=false")
public class SecurityIT extends WebApiIT {

    /** A template with NO Authorization interceptor — simulates a token-less caller. */
    private final TestRestTemplate anonymous = new TestRestTemplate();

    private HttpStatus statusOf(String path) {
        ResponseEntity<String> resp =
            anonymous.getForEntity(getBaseUri() + path, String.class);
        return HttpStatus.valueOf(resp.getStatusCode().value());
    }

    @Test
    public void tokenlessRequestsRejectedWhenAnonymousDisabled() {
        assertEquals(HttpStatus.UNAUTHORIZED, statusOf("/user"));
        assertEquals(HttpStatus.UNAUTHORIZED, statusOf("/cohortdefinition"));
        assertEquals(HttpStatus.UNAUTHORIZED, statusOf("/source/sources"));
    }

    @Test
    public void bootstrapEndpointsStayOpen() {
        assertEquals(HttpStatus.OK, statusOf("/info"));
        assertEquals(HttpStatus.OK, statusOf("/auth/providers"));
    }
}
