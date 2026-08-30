package org.ohdsi.webapi.security.authz.access;

/**
 * Spring Data interface projection for entity access query results.
 * Used by access repositories to return (entityId, accessType) tuples
 * without loading full entities.
 */
public interface EntityAccessProjection {
    Long getEntityId();
    AccessType getAccessType();
}
