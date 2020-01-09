package org.ohdsi.webapi.cohortsample;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface CohortSampleRepository extends CrudRepository<CohortSample, Integer> {
    List<CohortSample> findByCohortDefinitionIdAndSourceId(int cohortDefinitionId, int sourceId);
}
