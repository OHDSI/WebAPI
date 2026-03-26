package org.ohdsi.webapi.test;

import java.util.Collections;
import java.util.UUID;

import org.junit.AfterClass;
import org.ohdsi.webapi.security.authc.WebApiAuthenticationToken;
import org.ohdsi.webapi.security.identity.WebApiPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 
 *
 */
public abstract class AbstractSpringSecurity {

    public AbstractSpringSecurity() {
    }

    /**
     * Accepts a WebApiPrincipal (possibly mocked) to set to the authenticated
     * context
     * in SecurityContextHolder.getContext().setAuthentication().
     */
    protected static void setSubject(WebApiPrincipal principal) {
        clearSubject();
        if (principal == null)
            principal = WebApiPrincipal.ANONYMOUS;
        Authentication auth = WebApiAuthenticationToken.authenticated(principal, UUID.randomUUID(), Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    /**
     * Clear the current security context to avoid leaking authentication between
     * tests.
     */
    protected static void clearSubject() {
        SecurityContextHolder.clearContext();
    }

    @AfterClass
    public static void tearDownSecurity() {
        clearSubject();
    }
}
