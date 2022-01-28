package org.ohdsi.webapi.cohortcharacterization.repository;

import java.util.List;
import java.util.Optional;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CcRepository extends EntityGraphJpaRepository<CohortCharacterizationEntity, Long> {
    Optional<CohortCharacterizationEntity> findById(final Long id);

    @Query("SELECT cc FROM CohortCharacterizationEntity cc WHERE cc.name LIKE ?1 ESCAPE '\\'")
    List<CohortCharacterizationEntity> findAllByNameStartsWith(String pattern);

    Optional<CohortCharacterizationEntity> findByName(String name);
    
    @Query("SELECT COUNT(cc) FROM CohortCharacterizationEntity cc WHERE cc.name = :ccName and cc.id <> :ccId")
    int getCountCcWithSameName(@Param("ccId") Long ccId, @Param("ccName") String ccName);

    @Query("SELECT cc FROM CohortCharacterizationEntity cc JOIN cc.cohortDefinitions cd WHERE cd = ?1")
    List<CohortCharacterizationEntity> findByCohortDefinition(CohortDefinition cd);

    @Query("SELECT cc FROM CohortCharacterizationEntity cc JOIN cc.featureAnalyses fa WHERE fa = ?1")
    List<CohortCharacterizationEntity> findByFeatureAnalysis(FeAnalysisEntity feAnalysis);

    @Query("SELECT DISTINCT cc FROM CohortCharacterizationEntity cc JOIN FETCH cc.tags t WHERE lower(t.name) in :tagNames")
    List<CohortCharacterizationEntity> findByTags(@Param("tagNames") List<String> tagNames);
}
