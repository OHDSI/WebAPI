package org.ohdsi.webapi.cohortsample;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Repository of samples. This does not fetch any sample elements.
 */
@Component
public interface CohortSampleRepository extends CrudRepository<CohortSample, Integer> {
    List<CohortSample> findByCohortDefinitionIdAndSourceId(int cohortDefinitionId, int sourceId);
}
