package org.ohdsi.webapi.cohort;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface CohortRepository extends CrudRepository<CohortEntity, Long> {
	
	@Query("SELECT c FROM CohortEntity c WHERE c.cohortDefinitionId = ?1")
	public List<CohortEntity> getAllCohortsForId(Long id);

}
