package org.ohdsi.webapi.feanalysis;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.analysis.cohortcharacterization.design.CcResultType;
import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisType;
import org.ohdsi.circe.cohortdefinition.ConceptSet;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.conceptset.ConceptSetExport;
import org.ohdsi.webapi.feanalysis.domain.*;
import org.ohdsi.webapi.feanalysis.event.FeAnalysisChangedEvent;
import org.ohdsi.webapi.feanalysis.repository.FeAnalysisAggregateRepository;
import org.ohdsi.webapi.feanalysis.repository.FeAnalysisCriteriaRepository;
import org.ohdsi.webapi.feanalysis.repository.FeAnalysisEntityRepository;
import org.ohdsi.webapi.feanalysis.repository.FeAnalysisWithStringEntityRepository;
import org.ohdsi.webapi.source.SourceInfo;
import org.ohdsi.webapi.util.EntityUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.NotFoundException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.service.VocabularyService;

@Service
@Transactional(readOnly = true)
public class FeAnalysisServiceImpl extends AbstractDaoService implements FeAnalysisService {
    
    private final FeAnalysisEntityRepository analysisRepository;
    private final FeAnalysisCriteriaRepository criteriaRepository;
    private final FeAnalysisWithStringEntityRepository stringAnalysisRepository;
    private final VocabularyService vocabularyService;
    
    private final ApplicationEventPublisher eventPublisher;
    private FeAnalysisAggregateRepository aggregateRepository;

    private final EntityGraph defaultEntityGraph = EntityUtils.fromAttributePaths(
            "createdBy",
            "modifiedBy"
    );

    public FeAnalysisServiceImpl(
            final FeAnalysisEntityRepository analysisRepository,
            final FeAnalysisCriteriaRepository criteriaRepository, 
            final FeAnalysisWithStringEntityRepository stringAnalysisRepository,
            final VocabularyService vocabularyService,
            final FeAnalysisAggregateRepository aggregateRepository,
            final ApplicationEventPublisher eventPublisher) {
        this.analysisRepository = analysisRepository;
        this.criteriaRepository = criteriaRepository;
        this.stringAnalysisRepository = stringAnalysisRepository;
        this.vocabularyService = vocabularyService;
        this.aggregateRepository = aggregateRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Page<FeAnalysisEntity> getPage(final Pageable pageable) {
        return analysisRepository.findAll(pageable, defaultEntityGraph);
    }
    
    @Override
    public int getCountFeWithSameName(Integer id, String name){
        return analysisRepository.getCountFeWithSameName(id, name);
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
        return saveNew(analysis);
    }

    @Override
    public Optional<FeAnalysisEntity> findById(Integer id) {
        return analysisRepository.findById(id, defaultEntityGraph);
    }

    @Override
    public Optional<FeAnalysisEntity> findByName(String name) {
        return analysisRepository.findByName(name);
    }

    @Override
    @Transactional
    public FeAnalysisWithCriteriaEntity createCriteriaAnalysis(final FeAnalysisWithCriteriaEntity analysis) {
        FeAnalysisWithCriteriaEntity newAnalysis = newAnalysis(analysis);
        newAnalysis.setDesign(Collections.emptyList());
        final FeAnalysisWithCriteriaEntity entityWithMainFields = saveNew(newAnalysis);
        if (createOrUpdateConceptSetEntity(entityWithMainFields, analysis.getConceptSetEntity())) {
            analysisRepository.save(entityWithMainFields);
        }
        final List<FeAnalysisCriteriaEntity> criteriaList = createCriteriaListForAnalysis(entityWithMainFields, analysis.getDesign());
        entityWithMainFields.setDesign(criteriaList);
        return entityWithMainFields;
    }

    private boolean createOrUpdateConceptSetEntity(FeAnalysisWithCriteriaEntity analysis, FeAnalysisConcepsetEntity modifiedConceptSet) {

        if (Objects.nonNull(modifiedConceptSet)) {
            FeAnalysisConcepsetEntity concepsetEntity = Optional.ofNullable(analysis.getConceptSetEntity())
                    .orElseGet(FeAnalysisConcepsetEntity::new);
            concepsetEntity.setFeatureAnalysis(analysis);
            concepsetEntity.setRawExpression(modifiedConceptSet.getRawExpression());
            analysis.setConceptSetEntity(concepsetEntity);
            return true;
        } else {
            return false;
        }
    }

    private <T extends FeAnalysisEntity> T saveNew(T entity) {
        entity.setCreatedBy(getCurrentUser());
        entity.setCreatedDate(new Date());
        return analysisRepository.saveAndFlush(entity);
    }

    private FeAnalysisWithCriteriaEntity newAnalysis(final FeAnalysisWithCriteriaEntity analysis) {
      if (Objects.equals(analysis.getStatType(), CcResultType.PREVALENCE)) {
        return new FeAnalysisWithPrevalenceCriteriaEntity(analysis);
      } else if (Objects.equals(analysis.getStatType(), CcResultType.DISTRIBUTION)) {
        return new FeAnalysisWithDistributionCriteriaEntity(analysis);
      }
      throw new IllegalArgumentException();
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
          FeAnalysisWithCriteriaEntity<?> updatedWithCriteriaEntity = (FeAnalysisWithCriteriaEntity) updatedEntity,
                  savedWithCriteria = (FeAnalysisWithCriteriaEntity) savedEntity;
          removeFeAnalysisCriteriaEntities(savedWithCriteria, updatedWithCriteriaEntity);
          updatedWithCriteriaEntity.getDesign().forEach(criteria -> criteria.setFeatureAnalysis(savedWithCriteria));
          createOrUpdateConceptSetEntity((FeAnalysisWithCriteriaEntity) savedEntity, updatedWithCriteriaEntity.getConceptSetEntity());
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
        savedEntity.setModifiedBy(getCurrentUser());
        savedEntity.setModifiedDate(new Date());
        savedEntity = analysisRepository.save(savedEntity);
        eventPublisher.publishEvent(new FeAnalysisChangedEvent(savedEntity));
        return savedEntity;
    }

    private void removeFeAnalysisCriteriaEntities(FeAnalysisWithCriteriaEntity<?> original, FeAnalysisWithCriteriaEntity<?> updated) {

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

    @Override
    @Transactional
    public void deleteAnalysis(int id) {
        deleteAnalysis(analysisRepository.findById(id).orElseThrow(() -> new RuntimeException("There is no Feature Analysis with id = " + id)));
    }
    
    @Override
    public List<String> getNamesLike(String name) {
        return analysisRepository.findAllByNameStartsWith(name).stream().map(FeAnalysisEntity::getName).collect(Collectors.toList());
    }

    @Override
    public List<ConceptSetExport> exportConceptSets(FeAnalysisWithCriteriaEntity<?> analysisEntity) {

        SourceInfo sourceInfo = new SourceInfo(vocabularyService.getPriorityVocabularySource());
        List<ConceptSet> conceptSets = analysisEntity.getConceptSets();
        return conceptSets.stream()
                .map(cs -> vocabularyService.exportConceptSet(cs, sourceInfo))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<? extends FeAnalysisEntity> findByDesignAndName(final FeAnalysisWithStringEntity withStringEntity, final String name) {
        return this.findByDesignAndPredicate(withStringEntity.getDesign(), f -> Objects.equals(f.getName(), name));
    }

    @Override
    public Optional<FeAnalysisEntity> findByCriteriaListAndCsAndDomainAndStat(List<? extends FeAnalysisCriteriaEntity> newCriteriaList, FeAnalysisWithCriteriaEntity<? extends FeAnalysisCriteriaEntity> newFeAnalysis) {
        Map<FeAnalysisWithCriteriaEntity, List<FeAnalysisCriteriaEntity>> feAnalysisEntityListMap = newCriteriaList.stream()
                .map(c -> criteriaRepository.findAllByExpressionString(c.getExpressionString()))
                .flatMap(List::stream).collect(Collectors.groupingBy(FeAnalysisCriteriaEntity::getFeatureAnalysis));
        return feAnalysisEntityListMap.entrySet().stream().filter(e -> {
            FeAnalysisWithCriteriaEntity feAnalysis = e.getKey();
            return checkCriteriaList(e.getValue(), newCriteriaList) &&
                    CollectionUtils.isEqualCollection(feAnalysis.getConceptSets(), newFeAnalysis.getConceptSets()) &&
                    feAnalysis.getDomain().equals(newFeAnalysis.getDomain()) &&
                    feAnalysis.getStatType().equals(newFeAnalysis.getStatType());
            }).findAny().map(Map.Entry::getKey);
    }

    private boolean checkCriteriaList(List<FeAnalysisCriteriaEntity> curCriteriaList, List<? extends FeAnalysisCriteriaEntity> newCriteriaList) {
        List<String> currentList = curCriteriaList.stream().map(FeAnalysisCriteriaEntity::getExpressionString).collect(Collectors.toList());
        List<String> newList = newCriteriaList.stream().map(FeAnalysisCriteriaEntity::getExpressionString).collect(Collectors.toList());
        return CollectionUtils.isEqualCollection(currentList, newList);
    }

    private Optional<? extends FeAnalysisEntity> findByDesignAndPredicate(final String design, final Predicate<FeAnalysisEntity> f) {
        List<? extends FeAnalysisEntity> detailsFromDb = stringAnalysisRepository.findByDesign(design);
        return detailsFromDb
                .stream()
                .filter(f)
                .findFirst();
    }
    

    private void checkEntityLocked(FeAnalysisEntity entity) {
        if (entity.getLocked() == Boolean.TRUE) {
            throw new IllegalArgumentException(String.format("Feature analysis %s is locked.", entity.getName()));
        }
    }

    @Override
    public List<FeAnalysisAggregateEntity> findAggregates() {

        return aggregateRepository.findAll();
    }
}
