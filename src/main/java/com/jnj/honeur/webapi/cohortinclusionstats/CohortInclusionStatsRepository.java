package com.jnj.honeur.webapi.cohortinclusionstats;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CohortInclusionStatsRepository extends CrudRepository<CohortInclusionStatsEntity, Long> {

    List<CohortInclusionStatsEntity> findByCohortDefinitionId(long id);
}
