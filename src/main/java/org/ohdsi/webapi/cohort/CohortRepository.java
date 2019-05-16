package org.ohdsi.webapi.cohort;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CohortRepository extends CrudRepository<CohortEntity, Long> {
	
	@Query("from CohortEntity where cohort_definition_id = ?1")
	public List<CohortEntity> getAllCohortsForId(Long id);

}
