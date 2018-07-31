package org.ohdsi.webapi.cohortcharacterization;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.ws.rs.NotFoundException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.standardized_analysis_api.Utils;
import org.ohdsi.standardized_analysis_api.cohortcharacterization.design.StandardFeatureAnalysisType;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetails;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetailsRepository;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.feanalysis.FeAnalysisEntity;
import org.ohdsi.webapi.feanalysis.FeAnalysisService;
import org.ohdsi.webapi.feanalysis.FeAnalysisWithCriteriaEntity;
import org.ohdsi.webapi.feanalysis.FeAnalysisWithCriteriaEntityRepository;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.ohdsi.webapi.shiro.management.Security;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CcServiceImpl implements CcService {
    
    private CcRepository repository;
    private Security security;
    private UserRepository userRepository;
    private CcParamRepository paramRepository;
    private FeAnalysisService analysisService;
    private CohortDefinitionRepository cohortRepository;
    private CohortDefinitionDetailsRepository detailsRepository;
    
    public CcServiceImpl(
            final CcRepository ccRepository,
            final Security security,
            final UserRepository userRepository,
            final CcParamRepository paramRepository,
            final FeAnalysisService analysisService,
            final CohortDefinitionRepository cohortRepository,
            final CohortDefinitionDetailsRepository detailsRepository) {
        this.repository = ccRepository;
        this.security = security;
        this.userRepository = userRepository;
        this.paramRepository = paramRepository;
        this.analysisService = analysisService;
        this.cohortRepository = cohortRepository;
        this.detailsRepository = detailsRepository;
    }
    
    @Override
    public CohortCharacterizationEntity createCc(final CohortCharacterizationEntity entity) {
        entity.setCreatedBy(getCurrentUser());
        entity.setCreatedAt(new Date());
        return saveCc(entity);
    }

    private CohortCharacterizationEntity saveCc(final CohortCharacterizationEntity entity) {
        final CohortCharacterizationEntity savedEntity = repository.save(entity);
        
        sortInnerEntities(savedEntity);
        
        final String serialized = this.serializeCc(savedEntity);
        savedEntity.setHashCode(serialized.hashCode());
        
        return repository.save(savedEntity);
    }

    private void sortInnerEntities(final CohortCharacterizationEntity savedEntity) {
        savedEntity.setFeatureAnalyses(new TreeSet<>(savedEntity.getFeatureAnalyses()));
    }

    private UserEntity getCurrentUser() { //TODO fix after security check
        return userRepository.findByLogin("admin@example.com"/*security.getSubject()*/);
    }

    @Override
    public CohortCharacterizationEntity updateCc(final CohortCharacterizationEntity entity) {
        final CohortCharacterizationEntity foundEntity = repository.findById(entity.getId())
                .orElseThrow(() -> new NotFoundException("CC entity isn't found"));
        
        updateLinkedFields(entity, foundEntity);

        entity.setUpdatedAt(new Date());
        entity.setUpdatedBy(getCurrentUser());
        
        return saveCc(entity);
    }

    private void updateLinkedFields(final CohortCharacterizationEntity entity, final CohortCharacterizationEntity foundEntity) {
        updateParams(entity, foundEntity);
        updateAnalyses(entity, foundEntity);
        updateCohorts(entity, foundEntity);
    }

    private void updateCohorts(final CohortCharacterizationEntity entity, final CohortCharacterizationEntity foundEntity) {
        foundEntity.getCohortDefinitions().clear();
        foundEntity.getCohortDefinitions().addAll(entity.getCohortDefinitions());
    }

    private void updateAnalyses(final CohortCharacterizationEntity entity, final CohortCharacterizationEntity foundEntity) {
        foundEntity.getFeatureAnalyses().clear();
        foundEntity.getFeatureAnalyses().addAll(entity.getFeatureAnalyses());
    }

    private void updateParams(final CohortCharacterizationEntity entity, final CohortCharacterizationEntity foundEntity) {
        updateOrCreateParams(entity, foundEntity);
        deleteParams(entity, foundEntity);
    }

    private void deleteParams(final CohortCharacterizationEntity entity, final CohortCharacterizationEntity foundEntity) {
        final Map<String, CcParamEntity> nameToParamFromInputMap = buildParamNameToParamMap(entity);
        final List<CcParamEntity> paramsForDelete = foundEntity.getParameters()
                .stream()
                .filter(parameter -> !nameToParamFromInputMap.containsKey(parameter.getName()))
                .collect(Collectors.toList());
        paramRepository.delete(paramsForDelete);
    }

    private void updateOrCreateParams(final CohortCharacterizationEntity entity, final CohortCharacterizationEntity foundEntity) {
        final Map<String, CcParamEntity> nameToParamFromDbMap = buildParamNameToParamMap(foundEntity);
        final List<CcParamEntity> paramsForCreateOrUpdate = new ArrayList<>();
        for (final CcParamEntity parameter : entity.getParameters()) {
            final CcParamEntity entityFromMap = nameToParamFromDbMap.get(parameter.getName());
            parameter.setCohortCharacterization(foundEntity);
            if (entityFromMap == null) {
                paramsForCreateOrUpdate.add(parameter);
            } else if (!StringUtils.equals(entityFromMap.getValue(), parameter.getValue())) {
                entityFromMap.setValue(parameter.getValue());
                paramsForCreateOrUpdate.add(entityFromMap);
            }
        }
        paramRepository.save(paramsForCreateOrUpdate);
    }

    @Override
    public CohortCharacterizationEntity importCc(final CohortCharacterizationEntity entity) {
        cleanIds(entity);

        final CohortCharacterizationEntity newCohortCharacterization = new CohortCharacterizationEntity();
        newCohortCharacterization.setName(entity.getName());
        final CohortCharacterizationEntity persistedCohortCharacterization = this.createCc(newCohortCharacterization);
        
        updateParams(entity, persistedCohortCharacterization);
        
        importCohorts(entity, persistedCohortCharacterization);
        importAnalyses(entity, persistedCohortCharacterization);

        final CohortCharacterizationEntity savedEntity = saveCc(persistedCohortCharacterization);
        
        return savedEntity;
    }

    @Override
    public String serializeCc(final Long id) {
        final CohortCharacterizationEntity cohortCharacterizationEntity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cohort characterization cannot be found by id: " + id));
        return this.serializeCc(cohortCharacterizationEntity);
    }
    
    @Override
    public String serializeCc(final CohortCharacterizationEntity cohortCharacterizationEntity) {
        try {
            return Utils.serialize(cohortCharacterizationEntity);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public CohortCharacterizationEntity findById(final Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cohort characterization with id: " + id + " cannot be found"));
    }
    
    @Override
    public CohortCharacterizationEntity findByIdWithLinkedEntities(final Long id) {
        return gatherLinkedEntities(findById(id));
    }

    private CohortCharacterizationEntity gatherLinkedEntities(final CohortCharacterizationEntity mainEntity) {
        mainEntity.setParameters(paramRepository.findAllByCohortCharacterization(mainEntity));
        mainEntity.setCohortDefinitions(cohortRepository.findAllByCohortCharacterizations(mainEntity));
        mainEntity.setFeatureAnalyses(analysisService.findByCohortCharacterization(mainEntity));
        return mainEntity;
    }

    @Override
    public Page<CohortCharacterizationEntity> getPageWithLinkedEntities(final Pageable pageable) {
        return this.getPage(pageable).map(this::gatherLinkedEntities);        
    }
    
    @Override
    public Page<CohortCharacterizationEntity> getPage(final Pageable pageable) {
        return repository.findAll(pageable);
    }
    

    private void importAnalyses(final CohortCharacterizationEntity entity, final CohortCharacterizationEntity persistedEntity) {
        final Map<String, FeAnalysisEntity> presetAnalysesMap = buildPresetAnalysisMap(entity);
        
        final Set<FeAnalysisEntity> entityAnalyses = new HashSet<>();
        
        for (final FeAnalysisEntity analysis : entity.getFeatureAnalyses()) {
            switch (analysis.getType()) {
                case CRITERIA_SET:
                    FeAnalysisWithCriteriaEntity criteriaAnalysis = (FeAnalysisWithCriteriaEntity)analysis;
                    entityAnalyses.add(analysisService.createCriteriaAnalysis(criteriaAnalysis));
                    break;
                case PRESET:
                    entityAnalyses.add(presetAnalysesMap.get(analysis.getDesign()));
                    break;
                case CUSTOM_FE:
                    entityAnalyses.add(analysisService.createAnalysis(analysis));
                    break;
                default:
                    throw new IllegalArgumentException("Analysis with type: " + analysis.getType() + " cannot be imported");
            }
        }

        persistedEntity.setFeatureAnalyses(entityAnalyses);
    }

    private Map<String, FeAnalysisEntity> buildPresetAnalysisMap(final CohortCharacterizationEntity entity) {
        return analysisService
                .findPresetAnalysesBySystemNames(gatherPresetAnalyses(entity))
                .stream()
                .collect(Collectors.toMap(FeAnalysisEntity::getDesign, Function.identity()));
    }

    private List<String> gatherPresetAnalyses(final CohortCharacterizationEntity entity) {
        return entity.getFeatureAnalyses()
                .stream()
                .filter(a -> StandardFeatureAnalysisType.PRESET.equals(a.getType()))
                .map(FeAnalysisEntity::getDesign)
                .map(v -> (String)v)
                .collect(Collectors.toList());
    }


    private void importCohorts(final CohortCharacterizationEntity entity, final CohortCharacterizationEntity persistedEntity) {
        final List<CohortDefinition> cohortList = entity.getCohortDefinitions().stream()
                .map(this::persistCohortOrLinkWithExisting)
                .collect(Collectors.toList());
        persistedEntity.setCohortDefinitions(cohortList);
    }

    private CohortDefinition persistCohortOrLinkWithExisting(final CohortDefinition cohort) {
        final CohortDefinitionDetails details = cohort.getDetails();
        return findCohortByExpressionHashcode(details).orElseGet(() -> {
            cohort.setCreatedBy("admin@example.com"/* TODO security.getSubject()*/);
            cohort.setCreatedDate(new Date());
            cohort.setDetails(null);
            final CohortDefinition savedCohort = cohortRepository.save(cohort);
            details.setCohortDefinition(savedCohort);
            savedCohort.setDetails(detailsRepository.save(details));
            return savedCohort;
        });
    }

    private Optional<CohortDefinition> findCohortByExpressionHashcode(final CohortDefinitionDetails details) {
        List<CohortDefinitionDetails> detailsFromDb = detailsRepository.findByHashCode(details.getHashCode());
        return detailsFromDb
                .stream()
                .filter(v -> ObjectUtils.equals(v.getExpression(), details.getExpression()))
                .findFirst()
                .map(CohortDefinitionDetails::getCohortDefinition);
    }
    
    private void cleanIds(final CohortCharacterizationEntity entity) {
        entity.setId(null);
        entity.getParameters().forEach(v -> v.setId(null));
        entity.getCohortDefinitions().forEach(v -> v.setId(null));
        entity.getFeatureAnalyses().forEach(v -> v.setId(null));
    }
    
    private Map<String, CcParamEntity> buildParamNameToParamMap(final CohortCharacterizationEntity foundEntity) {
        return foundEntity.getParameters()
                .stream()
                .collect(Collectors.toMap(CcParamEntity::getName, Function.identity()));
    }
}
