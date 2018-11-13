package org.ohdsi.webapi.pathway.repository;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.ohdsi.webapi.pathway.domain.PathwayEventCohort;

import java.util.List;

public interface PathwayEventCohortRepository extends EntityGraphJpaRepository<PathwayEventCohort, Long> {

    List<PathwayEventCohort> findAllByPathwayAnalysisId(Integer pathwayAnalysisId, EntityGraph source);
}
