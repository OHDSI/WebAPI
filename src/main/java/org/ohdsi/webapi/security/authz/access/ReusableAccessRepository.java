package org.ohdsi.webapi.security.authz.access;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for sec_reusable table
 */
@Repository
public interface ReusableAccessRepository extends JpaRepository<ReusableAccessEntity, ReusableAccessEntity.ReusableAccessId> {

    /**
     * Find all reusable access grants for a user via their roles.
     * Joins through UserRole to resolve role membership from a userId.
     * Returns (entityId, accessType) projections.
     */
    @Query("""
        SELECT ra.reusableId as entityId, ra.accessType as accessType
        FROM ReusableAccess ra
        JOIN UserRole ur ON ur.role.id = ra.roleId
        WHERE ur.user.id = :userId
    """)
    List<EntityAccessProjection> findAccessByUserId(@Param("userId") Long userId);

    /**
     * Find all reusable IDs created (owned) by this user.
     * Used to merge ownership as implicit WRITE access.
     */
    @Query("SELECT r.id FROM Reusable r WHERE r.createdBy.id = :userId")
    List<Integer> findOwnedReusableIds(@Param("userId") Long userId);

    /**
     * Find all role IDs that have a specific access type to a reusable.
     */
    @Query("SELECT ra.roleId FROM ReusableAccess ra WHERE ra.reusableId = :entityId AND ra.accessType = :accessType")
    List<Long> findRoleIdsByEntityIdAndAccessType(@Param("entityId") Long entityId, @Param("accessType") AccessType accessType);

    /**
     * Find all role IDs that have any access to a reusable.
     */
    @Query("SELECT DISTINCT ra.roleId FROM ReusableAccess ra WHERE ra.reusableId = :entityId")
    List<Long> findRoleIdsByEntityId(@Param("entityId") Long entityId);

    /**
     * Delete a specific access grant.
     */
    void deleteByRoleIdAndReusableIdAndAccessType(Long roleId, Long reusableId, AccessType accessType);
}
