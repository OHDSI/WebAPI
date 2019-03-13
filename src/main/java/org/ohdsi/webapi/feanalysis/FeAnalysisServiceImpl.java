package org.ohdsi.webapi.feanalysis;

import java.util.*;
import java.util.stream.Collectors;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisType;
import org.ohdsi.webapi.cohortcharacterization.CcResultType;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.feanalysis.domain.*;
import org.ohdsi.webapi.feanalysis.repository.FeAnalysisCriteriaRepository;
import org.ohdsi.webapi.feanalysis.repository.FeAnalysisEntityRepository;
import org.ohdsi.webapi.feanalysis.repository.FeAnalysisWithStringEntityRepository;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.util.EntityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.NotFoundException;

@Service
@Transactional(readOnly = true)
public class FeAnalysisServiceImpl extends AbstractDaoService implements FeAnalysisService {
    
    private FeAnalysisEntityRepository analysisRepository;
    private FeAnalysisCriteriaRepository criteriaRepository;
    private FeAnalysisWithStringEntityRepository stringAnalysisRepository;

    private final EntityGraph defaultEntityGraph = EntityUtils.fromAttributePaths(
            "createdBy",
            "modifiedBy"
    );

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
        return analysisRepository.findAll(pageable, defaultEntityGraph);
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
    @Transactional
    public FeAnalysisWithCriteriaEntity createCriteriaAnalysis(final FeAnalysisWithCriteriaEntity analysis) {
        FeAnalysisWithCriteriaEntity newAnalysis = newAnalysis(analysis);
        newAnalysis.setDesign(Collections.emptyList());
        final FeAnalysisWithCriteriaEntity entityWithMainFields = saveNew(newAnalysis);
        final List<FeAnalysisCriteriaEntity> criteriaList = createCriteriaListForAnalysis(entityWithMainFields, analysis.getDesign());
        entityWithMainFields.setDesign(criteriaList);
        return entityWithMainFields;
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
        UserEntity user = getCurrentUser();

        checkEntityLocked(savedEntity);
        savedEntity.setDescr(updatedEntity.getDescr());
        if (savedEntity instanceof FeAnalysisWithCriteriaEntity && updatedEntity instanceof FeAnalysisWithCriteriaEntity) {
          FeAnalysisWithCriteriaEntity<?> updatedWithCriteriaEntity = (FeAnalysisWithCriteriaEntity) updatedEntity,
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
        savedEntity.setModifiedBy(user);
        savedEntity.setModifiedDate(new Date());
        return analysisRepository.save(savedEntity);
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

    private void checkEntityLocked(FeAnalysisEntity entity) {
        if (entity.getLocked() == Boolean.TRUE) {
            throw new IllegalArgumentException(String.format("Feature analysis %s is locked.", entity.getName()));
        }
    }
}
