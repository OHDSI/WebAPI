package org.ohdsi.webapi.feanalysis;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisWithCriteriaEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisWithStringEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FeAnalysisService {

    Page<FeAnalysisEntity> getPage(final Pageable pageable);

    List<FeAnalysisEntity> findPresetAnalysisByFeAnalysisName(List<String> analysisNames);

    List<FeAnalysisWithStringEntity> findPresetAnalysesBySystemNames(Collection<String> names);

    FeAnalysisEntity createAnalysis(FeAnalysisEntity analysis);

    FeAnalysisWithCriteriaEntity createCriteriaAnalysis(FeAnalysisWithCriteriaEntity analysis);

    Set<FeAnalysisEntity> findByCohortCharacterization(CohortCharacterizationEntity cohortCharacterization);

    List<FeAnalysisEntity> findAllPresetAnalyses();
}
