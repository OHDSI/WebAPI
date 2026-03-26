package org.ohdsi.webapi.security.authz.access;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for sec_cohort_definition table
 */
@Repository
public interface CohortDefinitionAccessRepository extends JpaRepository<CohortDefinitionAccessEntity, CohortDefinitionAccessEntity.CohortDefinitionAccessId> {

    /**
     * Check if a user has specific access to a cohort definition
     */
    @Query("""
        SELECT CASE WHEN COUNT(ca) > 0 THEN true ELSE false END
        FROM CohortDefinitionAccess ca
        JOIN UserRole ur ON ur.role.id = ca.roleId
        WHERE ur.user.id = :userId
        AND ca.cohortDefinitionId = :cohortDefinitionId
        AND ca.accessType = :accessType        
    """)
    boolean hasAccess(@Param("userId") Long userId, 
                      @Param("cohortDefinitionId") Long cohortDefinitionId,
                      @Param("accessType") AccessType accessType);

    /**
     * Get the owner (created_by_id) of a cohort definition
     */
    @Query("SELECT cd.createdBy.id FROM CohortDefinition cd WHERE cd.id = :cohortDefinitionId")
    Long getCreatedById(@Param("cohortDefinitionId") Long cohortDefinitionId);

    /**
     * Find all cohort definition access grants for a user via their roles.
     * Joins through UserRole to resolve role membership from a userId.
     * Returns (entityId, accessType) projections.
     */
    @Query("""
        SELECT ca.cohortDefinitionId as entityId, ca.accessType as accessType
        FROM CohortDefinitionAccess ca
        JOIN UserRole ur ON ur.role.id = ca.roleId
        WHERE ur.user.id = :userId
    """)
    List<EntityAccessProjection> findAccessByUserId(@Param("userId") Long userId);

    /**
     * Find all cohort definition IDs created (owned) by this user.
     * Used to merge ownership as implicit WRITE access.
     */
    @Query("SELECT cd.id FROM CohortDefinition cd WHERE cd.createdBy.id = :userId")
    List<Integer> findOwnedCohortDefinitionIds(@Param("userId") Long userId);
}
