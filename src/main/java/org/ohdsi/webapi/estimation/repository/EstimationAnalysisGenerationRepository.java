package org.ohdsi.webapi.estimation.repository;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.ohdsi.webapi.estimation.domain.EstimationGenerationEntity;

import java.util.List;

public interface EstimationAnalysisGenerationRepository extends EntityGraphJpaRepository<EstimationGenerationEntity, Long> {

  List<EstimationGenerationEntity> findByEstimationAnalysisId(Integer estimationAnalysisId, EntityGraph entityGraph);
}
