package org.ohdsi.webapi.generationcache;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.ohdsi.webapi.source.Source;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

interface GenerationCacheRepository extends EntityGraphJpaRepository<GenerationCache, Integer> {

    GenerationCache findByTypeAndAndDesignHashAndSource(CacheableGenerationType type, Integer designHash, Source source, EntityGraph entityGraph);
    List<GenerationCache> findAllByCreatedDateBefore(Date date, EntityGraph entityGraph);
    List<GenerationCache> findAllBySourceSourceId(int source);
}
