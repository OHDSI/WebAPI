package org.ohdsi.webapi.security.authz.access;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for sec_pathway_analysis table */
@Repository
public interface PathwayAccessRepository extends JpaRepository<PathwayAccessEntity, PathwayAccessEntity.PathwayAccessId> {

    @Query("""
        SELECT pa.pathwayAnalysisId as entityId, pa.accessType as accessType
        FROM PathwayAccess pa
        JOIN UserRole ur ON ur.role.id = pa.roleId
        WHERE ur.user.id = :userId
    """)
    List<EntityAccessProjection> findAccessByUserId(@Param("userId") Long userId);

    @Query("SELECT pa.id FROM PathwayAnalysis pa WHERE pa.createdBy.id = :userId")
    List<Integer> findOwnedPathwayAnalysisIds(@Param("userId") Long userId);

    /**
     * Find all role IDs that have a specific access type to a pathway analysis.
     */
    @Query("SELECT pa.roleId FROM PathwayAccess pa WHERE pa.pathwayAnalysisId = :entityId AND pa.accessType = :accessType")
    List<Long> findRoleIdsByEntityIdAndAccessType(@Param("entityId") Long entityId, @Param("accessType") AccessType accessType);

    /**
     * Find all role IDs that have any access to a pathway analysis.
     */
    @Query("SELECT DISTINCT pa.roleId FROM PathwayAccess pa WHERE pa.pathwayAnalysisId = :entityId")
    List<Long> findRoleIdsByEntityId(@Param("entityId") Long entityId);

    /**
     * Delete a specific access grant.
     */
    void deleteByRoleIdAndPathwayAnalysisIdAndAccessType(Long roleId, Long pathwayAnalysisId, AccessType accessType);
}
