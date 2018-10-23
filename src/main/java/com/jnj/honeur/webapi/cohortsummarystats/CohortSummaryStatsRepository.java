package com.jnj.honeur.webapi.cohortsummarystats;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CohortSummaryStatsRepository extends CrudRepository<CohortSummaryStatsEntity, Long> {

    List<CohortSummaryStatsEntity> findByCohortDefinitionId(long id);
}
