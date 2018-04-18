package com.jnj.honeur.webapi.cohortsummarystats;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CohortSummaryStatsRepository extends CrudRepository<CohortSummaryStatsEntity, Long> {

    @Query("from CohortSummaryStatsEntity where cohort_definition_id = ?1")
    List<CohortSummaryStatsEntity> getAllCohortInclusionSummaryStatsForId(long id);
}
