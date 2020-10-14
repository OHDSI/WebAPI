package org.ohdsi.webapi.cohortsample;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Repository of samples. This does not fetch any sample elements.
 */
@Component
public interface CohortSampleRepository extends CrudRepository<CohortSample, Integer> {
	@Query("SELECT c FROM CohortSample c LEFT JOIN FETCH c.createdBy WHERE c.id = :id")
	CohortSample findById(@Param("id") int cohortSampleId);

	@Query("SELECT c FROM CohortSample c LEFT JOIN FETCH c.createdBy WHERE c.cohortDefinitionId = :cohortDefinitionId AND c.sourceId = :sourceId")
	List<CohortSample> findByCohortDefinitionIdAndSourceId(@Param("cohortDefinitionId") int cohortDefinitionId, @Param("sourceId") int sourceId);

	@Query("SELECT count(c.id) FROM CohortSample c WHERE c.cohortDefinitionId = :cohortDefinitionId")
	int countSamples(@Param("cohortDefinitionId") int cohortDefinitionId);

	@Query("SELECT count(c.id) FROM CohortSample c WHERE c.cohortDefinitionId = :cohortDefinitionId AND c.sourceId = :sourceId")
	int countSamples(@Param("cohortDefinitionId") int cohortDefinitionId, @Param("sourceId") int sourceId);
}
