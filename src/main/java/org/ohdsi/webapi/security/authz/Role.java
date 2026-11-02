package org.ohdsi.webapi.security.authz;

import java.util.Comparator;

/**
 * Core Role domain object.
 */
public record Role(
    Long id,
    String name,
    boolean systemRole,
    boolean defaultImported) implements Comparable<Role> {

  private static final Comparator<Role> ROLE_ORDER = Comparator.comparing(
      Role::id,
      Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(Role::name);

  @Override
  public int compareTo(Role o) {
    return ROLE_ORDER.compare(this, o);
  }

  /**
   * Converts a RoleEntity to the Role domain object.
   */
  public static Role fromEntity(RoleEntity entity) {
    return fromEntity(entity, false);
  }

  /**
   * Converts a RoleEntity to the Role domain object with defaultImorted
   * indicator.
   */
  public static Role fromEntity(RoleEntity entity, boolean defaultImported) {
    return new Role(
        entity.getId(),
        entity.getName(),
        Boolean.TRUE.equals(entity.isSystemRole()),
        false);
  }
}
