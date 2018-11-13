package org.ohdsi.webapi.feanalysis.repository;

import org.ohdsi.analysis.cohortcharacterization.design.FeatureAnalysisType;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;

import java.util.List;

public interface FeAnalysisEntityRepository extends BaseFeAnalysisEntityRepository<FeAnalysisEntity> {

    List<FeAnalysisEntity> findAllByTypeAndRawDesignIn(FeatureAnalysisType type, List<String> designList);
}
