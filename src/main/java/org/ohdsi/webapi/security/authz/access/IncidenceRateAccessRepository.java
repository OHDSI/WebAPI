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
}
