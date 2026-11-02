package org.ohdsi.webapi.security.authz.access;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for sec_cohort_characterization table
 */
@Repository
public interface CohortCharacterizationAccessRepository extends JpaRepository<CohortCharacterizationAccessEntity, CohortCharacterizationAccessEntity.CohortCharacterizationAccessId> {

    /**
     * Find all cohort characterization access grants for a user via their roles.
     * Joins through UserRole to resolve role membership from a userId.
     * Returns (entityId, accessType) projections.
     */
    @Query("""
        SELECT ca.cohortCharacterizationId as entityId, ca.accessType as accessType
        FROM CohortCharacterizationAccess ca
        JOIN UserRole ur ON ur.role.id = ca.roleId
        WHERE ur.user.id = :userId
    """)
    List<EntityAccessProjection> findAccessByUserId(@Param("userId") Long userId);

    /**
     * Find all cohort characterization IDs created (owned) by this user.
     * Used to merge ownership as implicit WRITE access.
     */
    @Query("SELECT cc.id FROM CohortCharacterizationEntity cc WHERE cc.createdBy.id = :userId")
    List<Long> findOwnedCohortCharacterizationIds(@Param("userId") Long userId);

    /**
     * Find all role IDs that have a specific access type to a cohort characterization.
     */
    @Query("SELECT ca.roleId FROM CohortCharacterizationAccess ca WHERE ca.cohortCharacterizationId = :entityId AND ca.accessType = :accessType")
    List<Long> findRoleIdsByEntityIdAndAccessType(@Param("entityId") Long entityId, @Param("accessType") AccessType accessType);

    /**
     * Find all role IDs that have any access to a cohort characterization.
     */
    @Query("SELECT DISTINCT ca.roleId FROM CohortCharacterizationAccess ca WHERE ca.cohortCharacterizationId = :entityId")
    List<Long> findRoleIdsByEntityId(@Param("entityId") Long entityId);

    /**
     * Delete a specific access grant.
     */
    void deleteByRoleIdAndCohortCharacterizationIdAndAccessType(Long roleId, Long cohortCharacterizationId, AccessType accessType);
}
