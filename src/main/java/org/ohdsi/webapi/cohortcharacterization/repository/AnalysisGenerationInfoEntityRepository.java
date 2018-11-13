package org.ohdsi.webapi.cohortcharacterization.repository;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.ohdsi.webapi.common.generation.AnalysisGenerationInfoEntity;

public interface AnalysisGenerationInfoEntityRepository extends EntityGraphJpaRepository<AnalysisGenerationInfoEntity, Integer> {
}
