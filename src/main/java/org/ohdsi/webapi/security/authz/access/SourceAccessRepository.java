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
}
