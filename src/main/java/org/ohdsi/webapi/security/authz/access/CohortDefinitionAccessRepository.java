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

    /**
     * Find all role IDs that have a specific access type to a cohort definition.
     */
    @Query("SELECT ca.roleId FROM CohortDefinitionAccess ca WHERE ca.cohortDefinitionId = :entityId AND ca.accessType = :accessType")
    List<Long> findRoleIdsByEntityIdAndAccessType(@Param("entityId") Long entityId, @Param("accessType") AccessType accessType);

    /**
     * Find all role IDs that have any access to a cohort definition.
     */
    @Query("SELECT DISTINCT ca.roleId FROM CohortDefinitionAccess ca WHERE ca.cohortDefinitionId = :entityId")
    List<Long> findRoleIdsByEntityId(@Param("entityId") Long entityId);

    /**
     * Delete a specific access grant.
     */
    void deleteByRoleIdAndCohortDefinitionIdAndAccessType(Long roleId, Long cohortDefinitionId, AccessType accessType);
}
