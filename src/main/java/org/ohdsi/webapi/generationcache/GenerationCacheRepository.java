package org.ohdsi.webapi.generationcache;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.ohdsi.webapi.source.Source;

public interface GenerationCacheRepository extends EntityGraphJpaRepository<GenerationCache, Integer> {

    GenerationCache findByTypeAndAndDesignHashAndSource(CacheableGenerationType type, String designCache, Source source, EntityGraph entityGraph);
}
