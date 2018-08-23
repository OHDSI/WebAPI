package org.ohdsi.webapi.feanalysis;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.ohdsi.standardized_analysis_api.cohortcharacterization.design.StandardFeatureAnalysisType;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisCriteriaEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisWithCriteriaEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisWithStringEntity;
import org.ohdsi.webapi.feanalysis.repository.FeAnalysisCriteriaRepository;
import org.ohdsi.webapi.feanalysis.repository.FeAnalysisEntityRepository;
import org.ohdsi.webapi.feanalysis.repository.FeAnalysisWithStringEntityRepository;
import org.springframework.stereotype.Service;

@Service
public class FeAnalysisServiceImpl implements FeAnalysisService {
    
    private FeAnalysisEntityRepository analysisRepository;
    private FeAnalysisCriteriaRepository criteriaRepository;
    private FeAnalysisWithStringEntityRepository stringAnalysisRepository;
    
    public FeAnalysisServiceImpl(
            final FeAnalysisEntityRepository analysisRepository,
            final FeAnalysisCriteriaRepository criteriaRepository, 
            final FeAnalysisWithStringEntityRepository stringAnalysisRepository) {
        this.analysisRepository = analysisRepository;
        this.criteriaRepository = criteriaRepository;
        this.stringAnalysisRepository = stringAnalysisRepository;
    }
    
    @Override
    public List<FeAnalysisWithStringEntity> findPresetAnalysesBySystemNames(Collection<String> names) {
        return stringAnalysisRepository.findByDesignIn(names);
    }

    @Override
    public FeAnalysisEntity createAnalysis(final FeAnalysisEntity analysis) {
        return analysisRepository.save(analysis);
    }

    @Override
    public FeAnalysisWithCriteriaEntity createCriteriaAnalysis(final FeAnalysisWithCriteriaEntity analysis) {
        final FeAnalysisWithCriteriaEntity entityWithMainFields = analysisRepository.save(new FeAnalysisWithCriteriaEntity(analysis));
        final List<FeAnalysisCriteriaEntity> criteriaList = createCriteriaListForAnalysis(entityWithMainFields, analysis.getDesign());
        entityWithMainFields.setDesign(criteriaList);
        return entityWithMainFields;
    }

    private List<FeAnalysisCriteriaEntity> createCriteriaListForAnalysis(final FeAnalysisWithCriteriaEntity analysis, final List<FeAnalysisCriteriaEntity> design) {
        return design.stream()
                .peek(criteria -> criteria.setFeatureAnalysis(analysis))
                .map(criteria -> criteriaRepository.save(criteria))
                .collect(Collectors.toList());
    }
    
    @Override
    public Set<FeAnalysisEntity> findByCohortCharacterization(final CohortCharacterizationEntity cohortCharacterization) {
        return analysisRepository.findAllByCohortCharacterizations(cohortCharacterization);
    }
    
    @Override
    public List<FeAnalysisEntity> findAllPresetAnalyses() {
        return analysisRepository.findAllByType(StandardFeatureAnalysisType.PRESET);
    }
}
