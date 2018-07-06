package com.jnj.honeur.webapi.cohortfeatures;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CohortFeaturesRepository extends CrudRepository<CohortFeaturesEntity, Long> {

    List<CohortFeaturesEntity> findByCohortDefinitionId(long id);
}
