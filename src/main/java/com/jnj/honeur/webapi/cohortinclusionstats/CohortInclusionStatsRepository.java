package com.jnj.honeur.webapi.cohortinclusionstats;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CohortInclusionStatsRepository extends CrudRepository<CohortInclusionStatsEntity, Long> {

    @Query("from CohortInclusionStatsEntity where cohort_definition_id = ?1")
    List<CohortInclusionStatsEntity> getAllCohortInclusionStatsForId(long id);
}
