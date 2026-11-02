package org.ohdsi.webapi.security.authz.access;

import java.util.List;

import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.security.authz.access.EntityAccessProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SourceAccessRepository extends JpaRepository<SourceAccessEntity, SourceAccessEntity.SourceAccessId> {
 
  /**
   * Find all source access grants for a user via their roles.
   * Joins through UserRole to resolve role membership from a userId.
   * Returns (entityId, accessType) projections.
   */
  @Query("""
      SELECT sa.sourceId as entityId, sa.accessType as accessType
      FROM SourceAccess sa
      JOIN UserRole ur ON ur.role.id = sa.roleId
      WHERE ur.user.id = :userId
  """)
  List<EntityAccessProjection> findAccessByUserId(@Param("userId") Long userId);

  /**
   * Find all role IDs that have a specific access type to a source.
   */
  @Query("SELECT sa.roleId FROM SourceAccess sa WHERE sa.sourceId = :entityId AND sa.accessType = :accessType")
  List<Long> findRoleIdsByEntityIdAndAccessType(@Param("entityId") Long entityId, @Param("accessType") AccessType accessType);

  /**
   * Find all role IDs that have any access to a source.
   */
  @Query("SELECT DISTINCT sa.roleId FROM SourceAccess sa WHERE sa.sourceId = :entityId")
  List<Long> findRoleIdsByEntityId(@Param("entityId") Long entityId);

  /**
   * Delete a specific access grant.
   */
  void deleteByRoleIdAndSourceIdAndAccessType(Long roleId, Long sourceId, AccessType accessType);
}
