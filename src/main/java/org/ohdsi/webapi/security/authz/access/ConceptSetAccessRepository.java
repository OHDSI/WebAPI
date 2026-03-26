package org.ohdsi.webapi.security.authz.access;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for sec_concept_set table
 */
@Repository
public interface ConceptSetAccessRepository extends JpaRepository<ConceptSetAccessEntity, ConceptSetAccessEntity.ConceptSetAccessId> {

    /**
     * Find all concept set access grants for a user via their roles.
     * Joins through UserRole to resolve role membership from a userId.
     * Returns (entityId, accessType) projections.
     */
    @Query("""
        SELECT ca.conceptSetId as entityId, ca.accessType as accessType
        FROM ConceptSetAccess ca
        JOIN UserRole ur ON ur.role.id = ca.roleId
        WHERE ur.user.id = :userId
    """)
    List<EntityAccessProjection> findAccessByUserId(@Param("userId") Long userId);

    /**
     * Find all concept set IDs created (owned) by this user.
     * Used to merge ownership as implicit WRITE access.
     */
    @Query("SELECT cs.id FROM ConceptSet cs WHERE cs.createdBy.id = :userId")
    List<Integer> findOwnedConceptSetIds(@Param("userId") Long userId);

    /**
     * Get the owner (created_by_id) of a concept set
     */
    @Query("SELECT cs.createdBy.id FROM ConceptSet cs WHERE cs.id = :conceptSetId")
    Long getCreatedById(@Param("conceptSetId") Long conceptSetId);
}
