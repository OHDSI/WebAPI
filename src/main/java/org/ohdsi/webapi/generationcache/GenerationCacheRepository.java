package org.ohdsi.webapi.generationcache;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.ohdsi.webapi.source.Source;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

interface GenerationCacheRepository extends EntityGraphJpaRepository<GenerationCache, Integer> {

    GenerationCache findByTypeAndAndDesignHashAndSource(CacheableGenerationType type, String designCache, Source source, EntityGraph entityGraph);
    GenerationCache findByTypeAndSourceAndResultIdentifier(CacheableGenerationType type, Source source, Integer resultIdentifier, EntityGraph entityGraph);
    List<GenerationCache> findAllByCreatedDateBefore(Date date, EntityGraph entityGraph);
    List<GenerationCache> findAllBySourceSourceId(int source);

    @Query("SELECT MAX(gc.resultIdentifier) FROM GenerationCache gc WHERE gc.type = :type AND gc.source.sourceId = :sourceId")
    Integer findMaxResultIdentifier(@Param("type") CacheableGenerationType type, @Param("sourceId") Integer sourceId);
}
