package com.jnj.honeur.webapi.cohortfeaturesref;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CohortFeaturesRefRepository extends CrudRepository<CohortFeaturesRefEntity, Long> {

    List<CohortFeaturesRefEntity> findByCohortDefinitionId(long id);
}
