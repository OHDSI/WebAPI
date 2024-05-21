package org.ohdsi.webapi.cdmresults.repository;

import org.ohdsi.webapi.cdmresults.domain.CDMCacheEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CDMCacheRepository extends CrudRepository<CDMCacheEntity, Long> {
    @Query("select c from CDMCacheEntity c where c.sourceId = :sourceId and c.conceptId in :conceptIds")
    List<CDMCacheEntity> findBySourceAndConceptIds(@Param("sourceId") int sourceId, @Param("conceptIds") List<Integer> conceptIds);
}
