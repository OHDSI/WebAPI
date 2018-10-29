package org.ohdsi.webapi.pathway.repository;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaSpecificationExecutor;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;

public interface PathwayAnalysisEntityRepository extends EntityGraphJpaRepository<PathwayAnalysisEntity, Integer>, EntityGraphJpaSpecificationExecutor<PathwayAnalysisEntity> {
}
