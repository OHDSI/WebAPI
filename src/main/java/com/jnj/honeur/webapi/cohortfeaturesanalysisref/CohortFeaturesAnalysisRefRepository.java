package com.jnj.honeur.webapi.cohortfeaturesanalysisref;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CohortFeaturesAnalysisRefRepository extends CrudRepository<CohortFeaturesAnalysisRefEntity, Long> {

    List<CohortFeaturesAnalysisRefEntity> findByCohortDefinitionId(long id);
}
