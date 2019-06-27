package org.ohdsi.webapi.feanalysis.repository;

import org.ohdsi.webapi.feanalysis.domain.FeAnalysisCriteriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FeAnalysisCriteriaRepository extends JpaRepository<FeAnalysisCriteriaEntity, Long> {
    List<FeAnalysisCriteriaEntity> findAllByFeatureAnalysisId(Integer id);
    @Query("select fa from FeAnalysisCriteriaEntity AS fa JOIN FETCH fa.featureAnalysis where expression = ?1")
    List<FeAnalysisCriteriaEntity> findAllByExpressionString(String expression);
}