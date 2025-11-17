package org.ohdsi.webapi.mvc;

import org.ohdsi.webapi.common.sensitiveinfo.AbstractAdminService;
import org.ohdsi.webapi.shiro.management.Security;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

/**
 * Base class for Spring MVC controllers during Jersey migration.
 * Provides common functionality and utilities for migrated controllers.
 * Extends AbstractAdminService to inherit security helper methods (isSecured, isAdmin, isModerator).
 */
public abstract class AbstractMvcController extends AbstractAdminService {

    @Autowired
    protected Security security;

    /**
     * Get the current user's subject/login
     */
    protected String getCurrentUser() {
        return security.getSubject();
    }

    /**
     * Create an OK response with body
     */
    protected <T> ResponseEntity<T> ok(T body) {
        return ResponseEntity.ok(body);
    }

    /**
     * Create an OK response without body
     */
    protected ResponseEntity<Void> ok() {
        return ResponseEntity.ok().build();
    }

    /**
     * Create a NOT FOUND response
     */
    protected <T> ResponseEntity<T> notFound() {
        return ResponseEntity.notFound().build();
    }

    /**
     * Create a BAD REQUEST response
     */
    protected <T> ResponseEntity<T> badRequest() {
        return ResponseEntity.badRequest().build();
    }

    /**
     * Create a FORBIDDEN response
     */
    protected <T> ResponseEntity<T> forbidden() {
        return ResponseEntity.status(403).build();
    }

    /**
     * Create an UNAUTHORIZED response
     */
    protected <T> ResponseEntity<T> unauthorized() {
        return ResponseEntity.status(401).build();
    }

    /**
     * Create a NO CONTENT response
     */
    protected ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }
}
