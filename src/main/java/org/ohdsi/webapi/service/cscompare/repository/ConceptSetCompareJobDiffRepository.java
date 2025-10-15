package org.ohdsi.webapi.service.cscompare.repository;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.ohdsi.webapi.service.cscompare.entity.ConceptSetCompareJobDiffEntity;
import org.ohdsi.webapi.service.cscompare.entity.ConceptSetCompareJobEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConceptSetCompareJobDiffRepository extends EntityGraphJpaRepository<ConceptSetCompareJobDiffEntity, Integer> {

	Optional<ConceptSetCompareJobDiffEntity> findById(Integer id);

	@Query("SELECT diff FROM ConceptSetCompareJobDiffEntity diff WHERE diff.compareJob = :compareJob")
	List<ConceptSetCompareJobDiffEntity> findByCompareJob(@Param("compareJob") ConceptSetCompareJobEntity compareJob);

	@Query("SELECT diff FROM ConceptSetCompareJobDiffEntity diff WHERE diff.compareJob.id = :jobId")
	List<ConceptSetCompareJobDiffEntity> findByCompareJobId(@Param("jobId") Integer jobId);

	@Query("SELECT diff FROM ConceptSetCompareJobDiffEntity diff WHERE diff.conceptSetId = :conceptSetId")
	List<ConceptSetCompareJobDiffEntity> findByConceptSetId(@Param("conceptSetId") Integer conceptSetId);

	@Query("SELECT diff FROM ConceptSetCompareJobDiffEntity diff WHERE diff.conceptId = :conceptId")
	List<ConceptSetCompareJobDiffEntity> findByConceptId(@Param("conceptId") Integer conceptId);

	// Concept Set membership queries (CS1/CS2)
	@Query("SELECT diff FROM ConceptSetCompareJobDiffEntity diff WHERE diff.compareJob.id = :jobId AND diff.conceptInCS1Only > 0")
	List<ConceptSetCompareJobDiffEntity> findInCS1OnlyByJobId(@Param("jobId") Integer jobId);

	@Query("SELECT diff FROM ConceptSetCompareJobDiffEntity diff WHERE diff.compareJob.id = :jobId AND diff.conceptInCS2Only > 0")
	List<ConceptSetCompareJobDiffEntity> findInCS2OnlyByJobId(@Param("jobId") Integer jobId);

	@Query("SELECT diff FROM ConceptSetCompareJobDiffEntity diff WHERE diff.compareJob.id = :jobId AND diff.conceptInCS1AndCS2 > 0")
	List<ConceptSetCompareJobDiffEntity> findInBothCSByJobId(@Param("jobId") Integer jobId);

	// Name mismatch queries
	@Query("SELECT diff FROM ConceptSetCompareJobDiffEntity diff WHERE diff.compareJob.id = :jobId AND diff.nameMismatch = true")
	List<ConceptSetCompareJobDiffEntity> findNameMismatchesByJobId(@Param("jobId") Integer jobId);

	// Count queries
	@Query("SELECT COUNT(diff) FROM ConceptSetCompareJobDiffEntity diff WHERE diff.compareJob.id = :jobId")
	Long countByCompareJobId(@Param("jobId") Integer jobId);

	@Query("SELECT COUNT(diff) FROM ConceptSetCompareJobDiffEntity diff WHERE diff.compareJob.id = :jobId AND diff.conceptInCS1Only > 0")
	Long countInCS1OnlyByJobId(@Param("jobId") Integer jobId);

	@Query("SELECT COUNT(diff) FROM ConceptSetCompareJobDiffEntity diff WHERE diff.compareJob.id = :jobId AND diff.conceptInCS2Only > 0")
	Long countInCS2OnlyByJobId(@Param("jobId") Integer jobId);

	@Query("SELECT COUNT(diff) FROM ConceptSetCompareJobDiffEntity diff WHERE diff.compareJob.id = :jobId AND diff.conceptInCS1AndCS2 > 0")
	Long countInBothCSByJobId(@Param("jobId") Integer jobId);

	@Query("SELECT COUNT(diff) FROM ConceptSetCompareJobDiffEntity diff WHERE diff.compareJob.id = :jobId AND diff.nameMismatch = true")
	Long countNameMismatchesByJobId(@Param("jobId") Integer jobId);

	// Combined queries
	@Query("SELECT diff FROM ConceptSetCompareJobDiffEntity diff WHERE diff.compareJob.id = :jobId AND diff.conceptSetId = :conceptSetId")
	List<ConceptSetCompareJobDiffEntity> findByJobIdAndConceptSetId(@Param("jobId") Integer jobId, @Param("conceptSetId") Integer conceptSetId);

	@Query("SELECT diff FROM ConceptSetCompareJobDiffEntity diff " +
		"WHERE diff.compareJob.id = :jobId AND diff.conceptSetId = :conceptSetId AND diff.conceptInCS1Only > 0")
	List<ConceptSetCompareJobDiffEntity> findInCS1OnlyByJobIdAndConceptSetId(@Param("jobId") Integer jobId, @Param("conceptSetId") Integer conceptSetId);

	@Query("SELECT diff FROM ConceptSetCompareJobDiffEntity diff " +
		"WHERE diff.compareJob.id = :jobId AND diff.conceptSetId = :conceptSetId AND diff.conceptInCS2Only > 0")
	List<ConceptSetCompareJobDiffEntity> findInCS2OnlyByJobIdAndConceptSetId(@Param("jobId") Integer jobId, @Param("conceptSetId") Integer conceptSetId);

	@Query("SELECT diff FROM ConceptSetCompareJobDiffEntity diff " +
		"WHERE diff.compareJob.id = :jobId AND diff.conceptSetId = :conceptSetId AND diff.nameMismatch = true")
	List<ConceptSetCompareJobDiffEntity> findNameMismatchesByJobIdAndConceptSetId(@Param("jobId") Integer jobId, @Param("conceptSetId") Integer conceptSetId);

	// Summary statistics query
	@Query("SELECT new map(" +
		"COUNT(diff) as totalDiffs, " +
		"SUM(CASE WHEN diff.conceptInCS1Only > 0 THEN 1 ELSE 0 END) as cs1OnlyCount, " +
		"SUM(CASE WHEN diff.conceptInCS2Only > 0 THEN 1 ELSE 0 END) as cs2OnlyCount, " +
		"SUM(CASE WHEN diff.conceptInCS1AndCS2 > 0 THEN 1 ELSE 0 END) as bothCSCount, " +
		"SUM(CASE WHEN diff.nameMismatch = true THEN 1 ELSE 0 END) as nameMismatchCount) " +
		"FROM ConceptSetCompareJobDiffEntity diff WHERE diff.compareJob.id = :jobId")
	Object getSummaryStatsByJobId(@Param("jobId") Integer jobId);

	// Get all concept sets with differences for a job
	@Query("SELECT DISTINCT diff.conceptSetId FROM ConceptSetCompareJobDiffEntity diff WHERE diff.compareJob.id = :jobId")
	List<Integer> findDistinctConceptSetIdsByJobId(@Param("jobId") Integer jobId);

	// Delete operations
	@Query("DELETE FROM ConceptSetCompareJobDiffEntity diff WHERE diff.compareJob.id = :jobId")
	void deleteByCompareJobId(@Param("jobId") Integer jobId);

	@Query("DELETE FROM ConceptSetCompareJobDiffEntity diff WHERE diff.compareJob = :compareJob")
	void deleteByCompareJob(@Param("compareJob") ConceptSetCompareJobEntity compareJob);

	// Fetch with eager loading for performance
	@Query("SELECT diff FROM ConceptSetCompareJobDiffEntity diff " +
		"LEFT JOIN FETCH diff.compareJob " +
		"WHERE diff.compareJob.id = :jobId")
	List<ConceptSetCompareJobDiffEntity> findByCompareJobIdWithJob(@Param("jobId") Integer jobId);

	// Find differences for specific concept across all jobs
	@Query("SELECT diff FROM ConceptSetCompareJobDiffEntity diff " +
		"LEFT JOIN FETCH diff.compareJob " +
		"WHERE diff.conceptId = :conceptId " +
		"ORDER BY diff.compareJob.id DESC")
	List<ConceptSetCompareJobDiffEntity> findByConceptIdWithJob(@Param("conceptId") Integer conceptId);

	// Paginated query for large result sets
	@Query("SELECT diff FROM ConceptSetCompareJobDiffEntity diff " +
		"WHERE diff.compareJob.id = :jobId " +
		"ORDER BY diff.conceptSetId, diff.conceptId")
	List<ConceptSetCompareJobDiffEntity> findByCompareJobIdOrdered(@Param("jobId") Integer jobId);
}