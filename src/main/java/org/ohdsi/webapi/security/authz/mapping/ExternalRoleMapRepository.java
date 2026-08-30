package org.ohdsi.webapi.security.authz.mapping;

import java.util.Collection;
import java.util.List;
import org.ohdsi.webapi.security.authc.UserOrigin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data repository for ExternalRoleMapEntity.
 * Provides database access for external role mappings.
 */
@Repository
public interface ExternalRoleMapRepository extends JpaRepository<ExternalRoleMapEntity, Integer> {

  /**
   * Find all mappings for a specific authentication origin.
   *
   * @param origin the authentication origin (LDAP, OIDC, WINDOWS, etc.)
   * @return list of mappings for that origin
   */
  List<ExternalRoleMapEntity> findByOrigin(UserOrigin origin);

  /**
   * Find mappings for specific external claims within an origin.
   * Used during login to resolve external claims to roles.
   *
   * @param origin the authentication origin
   * @param externalClaims collection of claim values to resolve
   * @return list of role mappings matching the claims
   */
  List<ExternalRoleMapEntity> findByOriginAndExternalClaimIn(UserOrigin origin, Collection<String> externalClaims);

  /**
   * Count mappings for specific external claims within an origin.
   * Used to validate for duplicate claims before adding new mappings.
   *
   * @param origin the authentication origin
   * @param externalClaims collection of claim values to check
   * @return count of mappings matching the claims
   */
  int countByOriginAndExternalClaimIn(UserOrigin origin, Collection<String> externalClaims);

  /**
   * Delete mappings for a specific origin, claim, and role combination.
   * Used when cleaning up mappings.
   *
   * @param origin the authentication origin
   * @param externalClaim the claim value
   * @param roleId the WebAPI role ID
   * @return number of rows deleted
   */
  @Modifying
  @Query("DELETE FROM ExternalRoleMapEntity e WHERE e.origin = :origin AND e.externalClaim = :externalClaim AND e.role.id = :roleId")
  int deleteByOriginAndExternalClaimAndRoleId(UserOrigin origin, String externalClaim, Long roleId);

  /**
   * Delete all mappings for a specific origin and claim.
   * Useful when multiple roles were mapped to the same claim and need bulk deletion.
   *
   * @param origin the authentication origin
   * @param externalClaim the claim value
   */
  @Modifying
  @Query("DELETE FROM ExternalRoleMapEntity e WHERE e.origin = :origin AND e.externalClaim = :externalClaim")
  int deleteByOriginAndExternalClaim(UserOrigin origin, String externalClaim);

  /**
   * Delete all mappings for a specific origin.
   * Useful when re-configuring all mappings for an auth source.
   *
   * @param origin the authentication origin
   */
  @Modifying
  @Query("DELETE FROM ExternalRoleMapEntity e WHERE e.origin = :origin")
  int deleteByOrigin(UserOrigin origin);
}
