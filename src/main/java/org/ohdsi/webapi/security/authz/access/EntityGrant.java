package org.ohdsi.webapi.security.authz.access;

import java.util.Set;

/**
 * Represents a user's relationship to a specific authored entity.
 * Combines role-based access grants with ownership status.
 * <p>
 * Used for entity types where ownership is meaningful (e.g., CohortDefinition,
 * ConceptSet). Infrastructure entities like Source that have no ownership
 * semantics use a plain {@code Map<Long, Set<AccessType>>} instead.
 *
 * @param accessTypes The set of access types granted via roles (READ, WRITE)
 * @param isOwner     Whether the user created (owns) this entity
 */
public record EntityGrant(Set<AccessType> accessTypes, boolean isOwner) {

  /** A sentinel grant with no access and no ownership. */
  public static final EntityGrant NONE = new EntityGrant(Set.of(), false);

  /**
   * Check if this grant includes the specified access type.
   * Owner has full access (READ and WRITE) to the entity.
   * For non-owners, WRITE implies READ.
   */
  public boolean hasAccess(AccessType check) {
    if (isOwner) return true;
    return EntityGrant.hasAccess(check, accessTypes);
  }

  /**
   * This static function can be used by non-entiy grants
   * that still want to check if an AccessType passes the granted access types.
   * This centeralizes the logic of 'write implies read'
   */  
  public static boolean hasAccess(AccessType check, Set<AccessType> granted) {
    if (check == AccessType.READ) {
      return granted.contains(AccessType.READ) || granted.contains(AccessType.WRITE);
    }
    return granted.contains(check);
  }  
}
