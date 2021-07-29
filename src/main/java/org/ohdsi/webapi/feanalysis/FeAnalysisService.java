package org.ohdsi.webapi.feanalysis;

import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.conceptset.ConceptSetExport;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisAggregateEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisCriteriaEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisWithCriteriaEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisWithStringEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FeAnalysisService {

    Page<FeAnalysisEntity> getPage(final Pageable pageable);

    int getCountFeWithSameName(Integer id, String name);

    List<FeAnalysisWithStringEntity> findPresetAnalysesBySystemNames(Collection<String> names);

    FeAnalysisEntity createAnalysis(FeAnalysisEntity analysis);

    Optional<FeAnalysisEntity> findById(Integer id);

    Optional<FeAnalysisEntity> findByName(String name);

    FeAnalysisWithCriteriaEntity createCriteriaAnalysis(FeAnalysisWithCriteriaEntity analysis);

    Set<FeAnalysisEntity> findByCohortCharacterization(CohortCharacterizationEntity cohortCharacterization);

    List<FeAnalysisWithStringEntity> findAllPresetAnalyses();

    FeAnalysisEntity updateAnalysis(Integer feAnalysisId, FeAnalysisEntity convert);

    void deleteAnalysis(FeAnalysisEntity entity);
    
    void deleteAnalysis(int id);
    
    List<String> getNamesLike(String name);

    List<ConceptSetExport> exportConceptSets(FeAnalysisWithCriteriaEntity<?> analysisEntity);
    
    Optional<? extends FeAnalysisEntity> findByDesignAndName(FeAnalysisWithStringEntity withStringEntity, final String name);

    Optional<FeAnalysisEntity> findByCriteriaListAndCsAndDomainAndStat(List<? extends FeAnalysisCriteriaEntity> newCriteriaList, FeAnalysisWithCriteriaEntity<? extends FeAnalysisCriteriaEntity> feAnalysis);

    List<FeAnalysisAggregateEntity> findAggregates();
}
