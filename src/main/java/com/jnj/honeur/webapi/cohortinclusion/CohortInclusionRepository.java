package com.jnj.honeur.webapi.cohortinclusion;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CohortInclusionRepository extends CrudRepository<CohortInclusionEntity, Long> {

    List<CohortInclusionEntity> findByCohortDefinitionId(long id);
}
