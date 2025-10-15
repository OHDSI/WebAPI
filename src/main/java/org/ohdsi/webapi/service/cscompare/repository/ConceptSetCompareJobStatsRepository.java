package org.ohdsi.webapi.service.cscompare.repository;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.ohdsi.webapi.service.cscompare.entity.ConceptSetCompareJobStatsEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConceptSetCompareJobStatsRepository extends EntityGraphJpaRepository<ConceptSetCompareJobStatsEntity, Integer> {

	List<ConceptSetCompareJobStatsEntity> findByCompareJobId(Integer compareJobId);

	@Query("SELECT stats FROM ConceptSetCompareJobStatsEntity stats WHERE stats.compareJob.id = :compareJobId AND stats.conceptSetId = :conceptSetId")
	Optional<ConceptSetCompareJobStatsEntity> findByCompareJobIdAndConceptSetId(
		@Param("compareJobId") Integer compareJobId,
		@Param("conceptSetId") Integer conceptSetId
	);

	@Query("SELECT stats FROM ConceptSetCompareJobStatsEntity stats WHERE stats.compareJob.id = :compareJobId AND stats.hasDifferences = true")
	List<ConceptSetCompareJobStatsEntity> findByCompareJobIdWithDifferences(@Param("compareJobId") Integer compareJobId);

	@Query("SELECT COUNT(stats) FROM ConceptSetCompareJobStatsEntity stats WHERE stats.compareJob.id = :compareJobId AND stats.hasDifferences = true")
	Long countByCompareJobIdWithDifferences(@Param("compareJobId") Integer compareJobId);
}