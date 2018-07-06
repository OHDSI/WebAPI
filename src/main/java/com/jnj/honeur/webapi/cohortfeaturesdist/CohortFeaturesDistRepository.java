package com.jnj.honeur.webapi.cohortfeaturesdist;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CohortFeaturesDistRepository extends CrudRepository<CohortFeaturesDistEntity, Long> {

    List<CohortFeaturesDistEntity> findByCohortDefinitionId(long id);
}
