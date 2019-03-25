package org.ohdsi.webapi.cohortcharacterization.repository;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.ohdsi.webapi.common.generation.AnalysisGenerationInfoEntity;

import java.util.Optional;

public interface AnalysisGenerationInfoEntityRepository extends EntityGraphJpaRepository<AnalysisGenerationInfoEntity, Long> {
    Optional<AnalysisGenerationInfoEntity> findById(Long generationId);
}
