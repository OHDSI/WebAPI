package org.ohdsi.webapi.security.authz;

/**
 * Core Permission domain object.
 */
public record Permission(
    Long id,
    String value,
    String description
) {

    /**
     * Converts a PermissionEntity to the Permission domain object.
     */
    public static Permission fromEntity(PermissionEntity entity) {
        return new Permission(
            entity.getId(),
            entity.getValue(),
            entity.getDescription()
        );
    }
}
