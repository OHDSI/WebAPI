package org.ohdsi.webapi.feanalysis;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisType;
import org.ohdsi.webapi.cohortcharacterization.CcResultType;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.facets.FacetedSearchService;
import org.ohdsi.webapi.facets.FilteredPageRequest;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisConcepsetEntity;
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FeAnalysisServiceImpl implements FeAnalysisService {
    
    private final FeAnalysisEntityRepository analysisRepository;
    private final FeAnalysisCriteriaRepository criteriaRepository;
    private final FeAnalysisWithStringEntityRepository stringAnalysisRepository;
    private final FacetedSearchService facetedSearchService;

    public FeAnalysisServiceImpl(
            final FeAnalysisEntityRepository analysisRepository,
            final FeAnalysisCriteriaRepository criteriaRepository,
            final FeAnalysisWithStringEntityRepository stringAnalysisRepository,
            final FacetedSearchService facetedSearchService) {
        this.analysisRepository = analysisRepository;
        this.criteriaRepository = criteriaRepository;
        this.stringAnalysisRepository = stringAnalysisRepository;
        this.facetedSearchService = facetedSearchService;
    }

    @Override
    public Page<FeAnalysisEntity> getPage(final Pageable pageable) {
        return pageable instanceof FilteredPageRequest
                ? facetedSearchService.getPage((FilteredPageRequest) pageable, analysisRepository)
                : analysisRepository.findAll(pageable);
    }

  @Override
    public List<FeAnalysisWithStringEntity> findPresetAnalysesBySystemNames(Collection<String> names) {
        return stringAnalysisRepository.findByDesignIn(names);
    }

    @Override
    @Transactional
    public FeAnalysisEntity createAnalysis(final FeAnalysisEntity analysis) {
        if (analysis.getStatType() == null) {
            analysis.setStatType(CcResultType.PREVALENCE);
        }
        return analysisRepository.save(analysis);
    }

    @Override
    public Optional<FeAnalysisEntity> findById(Integer id) {
        return analysisRepository.findById(id);
    }

    @Override
    @Transactional
    public FeAnalysisWithCriteriaEntity createCriteriaAnalysis(final FeAnalysisWithCriteriaEntity analysis) {
        FeAnalysisWithCriteriaEntity newAnalysis = new FeAnalysisWithCriteriaEntity(analysis);
        newAnalysis.setDesign(Collections.emptyList());
        final FeAnalysisWithCriteriaEntity entityWithMainFields = analysisRepository.save(newAnalysis);
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
        if (savedEntity instanceof FeAnalysisWithCriteriaEntity && updatedEntity instanceof FeAnalysisWithCriteriaEntity) {
          FeAnalysisWithCriteriaEntity updatedWithCriteriaEntity = (FeAnalysisWithCriteriaEntity) updatedEntity,
                  savedWithCriteria = (FeAnalysisWithCriteriaEntity) savedEntity;
          removeFeAnalysisCriteriaEntities(savedWithCriteria, updatedWithCriteriaEntity);
          updatedWithCriteriaEntity.getDesign().forEach(criteria -> criteria.setFeatureAnalysis(savedWithCriteria));
          if (Objects.nonNull(updatedWithCriteriaEntity.getConceptSetEntity())) {
            FeAnalysisConcepsetEntity concepsetEntity = Optional.ofNullable(((FeAnalysisWithCriteriaEntity) savedEntity).getConceptSetEntity())
                    .orElseGet(FeAnalysisConcepsetEntity::new);
            concepsetEntity.setFeatureAnalysis(savedWithCriteria);
            concepsetEntity.setRawExpression(updatedWithCriteriaEntity.getConceptSetEntity().getRawExpression());
            savedWithCriteria.setConceptSetEntity(concepsetEntity);
          }
        }
        savedEntity.setDesign(updatedEntity.getDesign());
        if (Objects.nonNull(updatedEntity.getDomain())) {
            savedEntity.setDomain(updatedEntity.getDomain());
        }
        savedEntity.setLocked(updatedEntity.getLocked());
        if (StringUtils.isNotEmpty(updatedEntity.getName())) {
            savedEntity.setName(updatedEntity.getName());
        }
        if (updatedEntity.getStatType() != null) {
            savedEntity.setStatType(updatedEntity.getStatType());
        }
        if (Objects.nonNull(updatedEntity.getType())) {
            savedEntity.setType(updatedEntity.getType());
        }
        return analysisRepository.save(savedEntity);
    }

    private void removeFeAnalysisCriteriaEntities(FeAnalysisWithCriteriaEntity original, FeAnalysisWithCriteriaEntity updated) {

      List<FeAnalysisCriteriaEntity> removed = original.getDesign().stream()
              .filter(c -> updated.getDesign().stream().noneMatch(u -> Objects.equals(c.getId(), u.getId())))
              .collect(Collectors.toList());
      criteriaRepository.delete(removed);
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
