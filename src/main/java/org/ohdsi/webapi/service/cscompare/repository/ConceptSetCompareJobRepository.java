package org.ohdsi.webapi.service.cscompare.repository;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.ohdsi.webapi.service.cscompare.entity.ConceptSetCompareJobEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ConceptSetCompareJobRepository extends EntityGraphJpaRepository<ConceptSetCompareJobEntity, Integer> {

	Optional<ConceptSetCompareJobEntity> findById(Integer id);

	@Query("SELECT job FROM ConceptSetCompareJobEntity job WHERE job.source1Key = :source1Key AND job.source2Key = :source2Key")
	List<ConceptSetCompareJobEntity> findBySourceKeys(@Param("source1Key") String source1Key, @Param("source2Key") String source2Key);

	@Query("SELECT job FROM ConceptSetCompareJobEntity job WHERE job.source1Key = :sourceKey OR job.source2Key = :sourceKey")
	List<ConceptSetCompareJobEntity> findBySourceKey(@Param("sourceKey") String sourceKey);

	@Query("SELECT job FROM ConceptSetCompareJobEntity job WHERE job.createdFrom >= :fromDate AND job.createdTo <= :toDate")
	List<ConceptSetCompareJobEntity> findByCreatedDateRange(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

	@Query("SELECT job FROM ConceptSetCompareJobEntity job WHERE job.updatedFrom >= :fromDate AND job.updatedTo <= :toDate")
	List<ConceptSetCompareJobEntity> findByUpdatedDateRange(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

	@Query("SELECT job FROM ConceptSetCompareJobEntity job WHERE job.skipLocked = :skipLocked")
	List<ConceptSetCompareJobEntity> findBySkipLocked(@Param("skipLocked") Boolean skipLocked);

	@Query("SELECT job FROM ConceptSetCompareJobEntity job JOIN FETCH job.differences WHERE job.id = :jobId")
	Optional<ConceptSetCompareJobEntity> findByIdWithDifferences(@Param("jobId") Integer jobId);

	Optional<ConceptSetCompareJobEntity> findByExecutionId(Long executionId);
}