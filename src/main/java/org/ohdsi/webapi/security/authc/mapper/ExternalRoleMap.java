package org.ohdsi.webapi.security.authc.mapper;

import org.ohdsi.webapi.security.authz.Role;

/**
 * External role mapping domain object.
 * Represents a mapping between an external identity (group, claim) and a WebAPI role.
 */
public record ExternalRoleMap(
    Integer id,
    String origin,
    String externalClaim,
    Role role,
    String description) {

  /**
   * Converts an ExternalRoleMapEntity to the ExternalRoleMap domain object.
   */
  public static ExternalRoleMap fromEntity(ExternalRoleMapEntity entity) {
    return new ExternalRoleMap(
        entity.getId(),
        entity.getOrigin().toString(),
        entity.getExternalClaim(),
        Role.fromEntity(entity.getRole()),
        entity.getDescription());
  }
}
