package com.jnj.honeur.webapi.cohortinclusion;

import org.ohdsi.webapi.cohort.CohortEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CohortInclusionRepository extends CrudRepository<CohortInclusionEntity, Long> {

    @Query("from CohortInclusionEntity where cohort_definition_id = ?1")
    List<CohortInclusionEntity> getAllCohortInclusionsForId(long id);
}
