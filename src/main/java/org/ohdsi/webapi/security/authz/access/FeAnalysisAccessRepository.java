package org.ohdsi.webapi.security.authz.access;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for sec_fe_analysis table
 */
@Repository
public interface FeAnalysisAccessRepository extends JpaRepository<FeAnalysisAccessEntity, FeAnalysisAccessEntity.FeAnalysisAccessId> {

    /**
     * Find all feature-analysis access grants for a user via their roles.
     * Joins through UserRole to resolve role membership from a userId.
     * Returns (entityId, accessType) projections.
     */
    @Query("""
        SELECT fa.feAnalysisId as entityId, fa.accessType as accessType
        FROM FeAnalysisAccess fa
        JOIN UserRole ur ON ur.role.id = fa.roleId
        WHERE ur.user.id = :userId
    """)
    List<EntityAccessProjection> findAccessByUserId(@Param("userId") Long userId);

    /**
     * Find all feature analysis IDs created (owned) by this user.
     * Used to merge ownership as implicit WRITE access.
     */
    @Query("SELECT fa.id FROM FeAnalysisEntity fa WHERE fa.createdBy.id = :userId")
    List<Integer> findOwnedFeAnalysisIds(@Param("userId") Long userId);

    /**
     * Find all role IDs that have a specific access type to a feature analysis.
     */
    @Query("SELECT fa.roleId FROM FeAnalysisAccess fa WHERE fa.feAnalysisId = :entityId AND fa.accessType = :accessType")
    List<Long> findRoleIdsByEntityIdAndAccessType(@Param("entityId") Long entityId, @Param("accessType") AccessType accessType);

    /**
     * Find all role IDs that have any access to a feature analysis.
     */
    @Query("SELECT DISTINCT fa.roleId FROM FeAnalysisAccess fa WHERE fa.feAnalysisId = :entityId")
    List<Long> findRoleIdsByEntityId(@Param("entityId") Long entityId);

    /**
     * Delete a specific access grant.
     */
    void deleteByRoleIdAndFeAnalysisIdAndAccessType(Long roleId, Long feAnalysisId, AccessType accessType);
}
