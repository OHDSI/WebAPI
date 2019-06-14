package org.ohdsi.webapi.feanalysis.repository;

import org.ohdsi.webapi.feanalysis.domain.FeAnalysisCriteriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeAnalysisCriteriaRepository extends JpaRepository<FeAnalysisCriteriaEntity, Long> {
    List<FeAnalysisCriteriaEntity> findAllByFeatureAnalysisId(Integer id);
}