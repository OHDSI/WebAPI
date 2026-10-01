package org.ohdsi.webapi.security.authz.access;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for sec_ir_analysis table */
@Repository
public interface IncidenceRateAccessRepository extends JpaRepository<IncidenceRateAccessEntity, IncidenceRateAccessEntity.IncidenceRateAccessId> {

    @Query("""
        SELECT ia.irId as entityId, ia.accessType as accessType
        FROM IncidenceRateAccess ia
        JOIN UserRole ur ON ur.role.id = ia.roleId
        WHERE ur.user.id = :userId
    """)
    List<EntityAccessProjection> findAccessByUserId(@Param("userId") Long userId);

    @Query("SELECT ira.id FROM IncidenceRateAnalysis ira WHERE ira.createdBy.id = :userId")
    List<Integer> findOwnedIncidenceRateIds(@Param("userId") Long userId);

    /**
     * Find all role IDs that have a specific access type to an incidence rate analysis.
     */
    @Query("SELECT ia.roleId FROM IncidenceRateAccess ia WHERE ia.irId = :entityId AND ia.accessType = :accessType")
    List<Long> findRoleIdsByEntityIdAndAccessType(@Param("entityId") Long entityId, @Param("accessType") AccessType accessType);

    /**
     * Find all role IDs that have any access to an incidence rate analysis.
     */
    @Query("SELECT DISTINCT ia.roleId FROM IncidenceRateAccess ia WHERE ia.irId = :entityId")
    List<Long> findRoleIdsByEntityId(@Param("entityId") Long entityId);

    /**
     * Delete a specific access grant.
     */
    void deleteByRoleIdAndIrIdAndAccessType(Long roleId, Long irId, AccessType accessType);
}
