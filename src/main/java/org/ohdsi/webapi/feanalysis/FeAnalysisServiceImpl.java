package org.ohdsi.webapi.feanalysis;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisType;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisCriteriaEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisWithCriteriaEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisWithStringEntity;
import org.ohdsi.webapi.feanalysis.repository.FeAnalysisCriteriaRepository;
import org.ohdsi.webapi.feanalysis.repository.FeAnalysisEntityRepository;
import org.ohdsi.webapi.feanalysis.repository.FeAnalysisWithStringEntityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.NotFoundException;

@Service
@Transactional(readOnly = true)
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
    public Page<FeAnalysisEntity> getPage(final Pageable pageable) {
        return analysisRepository.findAll(pageable);
    }

  @Override
    public List<FeAnalysisWithStringEntity> findPresetAnalysesBySystemNames(Collection<String> names) {
        return stringAnalysisRepository.findByDesignIn(names);
    }

    @Override
    @Transactional
    public FeAnalysisEntity createAnalysis(final FeAnalysisEntity analysis) {
        return analysisRepository.save(analysis);
    }

    @Override
    public Optional<FeAnalysisEntity> findById(Integer id) {
        return analysisRepository.findById(id);
    }

    @Override
    @Transactional
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
    public List<FeAnalysisWithStringEntity> findAllPresetAnalyses() {
        return analysisRepository.findAllByType(StandardFeatureAnalysisType.PRESET).stream().map(a -> (FeAnalysisWithStringEntity) a).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FeAnalysisEntity updateAnalysis(Integer feAnalysisId, FeAnalysisEntity updatedEntity) {

        FeAnalysisEntity savedEntity = findById(feAnalysisId).orElseThrow(NotFoundException::new);
        checkEntityLocked(savedEntity);
        savedEntity.setDescr(updatedEntity.getDescr());
        savedEntity.setCohortCharacterizations(updatedEntity.getCohortCharacterizations());
        savedEntity.setDesign(updatedEntity.getDesign());
        if (Objects.nonNull(updatedEntity.getDomain())) {
            savedEntity.setDomain(updatedEntity.getDomain());
        }
        savedEntity.setLocked(updatedEntity.getLocked());
        if (StringUtils.isNotEmpty(updatedEntity.getName())) {
            savedEntity.setName(updatedEntity.getName());
        }
        savedEntity.setStatType(updatedEntity.getStatType());
        if (Objects.nonNull(updatedEntity.getType())) {
            savedEntity.setType(updatedEntity.getType());
        }
        return analysisRepository.save(savedEntity);
    }

    @Override
    @Transactional
    public void deleteAnalysis(FeAnalysisEntity entity) {
        checkEntityLocked(entity);
        analysisRepository.delete(entity);
    }

    private void checkEntityLocked(FeAnalysisEntity entity) {
        if (entity.getLocked() == Boolean.TRUE) {
            throw new IllegalArgumentException(String.format("Feature analysis %s is locked.", entity.getName()));
        }
    }
}
