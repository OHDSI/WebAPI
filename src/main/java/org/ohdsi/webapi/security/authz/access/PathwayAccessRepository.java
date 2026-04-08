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
}
