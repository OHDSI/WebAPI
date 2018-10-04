package org.ohdsi.webapi.pathway.repository;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.ohdsi.webapi.pathway.domain.PathwayTargetCohort;

import java.util.List;

public interface PathwayTargetCohortRepository extends EntityGraphJpaRepository<PathwayTargetCohort, Long> {

    List<PathwayTargetCohort> findAllByPathwayAnalysisId(Integer pathwayAnalysisId, EntityGraph source);
}
