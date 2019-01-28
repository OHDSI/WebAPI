package org.ohdsi.webapi.feanalysis;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisWithCriteriaEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisWithStringEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FeAnalysisService {

    Page<FeAnalysisEntity> getPage(final Pageable pageable);

    List<FeAnalysisWithStringEntity> findPresetAnalysesBySystemNames(Collection<String> names);

    FeAnalysisEntity createAnalysis(FeAnalysisEntity analysis);

    Optional<FeAnalysisEntity> findById(Integer id);

    FeAnalysisWithCriteriaEntity createCriteriaAnalysis(FeAnalysisWithCriteriaEntity analysis);

    Set<FeAnalysisEntity> findByCohortCharacterization(CohortCharacterizationEntity cohortCharacterization);

    List<FeAnalysisWithStringEntity> findAllPresetAnalyses();

    FeAnalysisEntity updateAnalysis(Integer feAnalysisId, FeAnalysisEntity convert);

    void deleteAnalysis(FeAnalysisEntity entity);
    
    void deleteAnalysis(int id);
}
