package com.jnj.honeur.webapi.cohortinclusionresult;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CohortInclusionResultRepository extends CrudRepository<CohortInclusionResultEntity, Long> {

    List<CohortInclusionResultEntity> findByCohortDefinitionId(long id);
}
