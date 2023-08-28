package org.ohdsi.webapi.cohortcharacterization;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.analysis.Utils;
import org.ohdsi.analysis.WithId;
import org.ohdsi.analysis.cohortcharacterization.design.CohortCharacterization;
import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisType;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.featureExtraction.FeatureExtraction;
import org.ohdsi.hydra.Hydra;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.JobInvalidator;
import org.ohdsi.webapi.cohortcharacterization.converter.SerializedCcToCcConverter;
import org.ohdsi.webapi.cohortcharacterization.domain.CcGenerationEntity;
import org.ohdsi.webapi.cohortcharacterization.domain.CcParamEntity;
import org.ohdsi.webapi.cohortcharacterization.domain.CcStrataConceptSetEntity;
import org.ohdsi.webapi.cohortcharacterization.domain.CcStrataEntity;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortcharacterization.dto.CcDistributionStat;
import org.ohdsi.webapi.cohortcharacterization.dto.CcExportDTO;
import org.ohdsi.webapi.cohortcharacterization.dto.CcPrevalenceStat;
import org.ohdsi.webapi.cohortcharacterization.dto.CcResult;
import org.ohdsi.webapi.cohortcharacterization.dto.CcShortDTO;
import org.ohdsi.webapi.cohortcharacterization.dto.CcVersionFullDTO;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;
import org.ohdsi.webapi.cohortcharacterization.dto.ExecutionResultRequest;
import org.ohdsi.webapi.cohortcharacterization.dto.ExportExecutionResultRequest;
import org.ohdsi.webapi.cohortcharacterization.dto.GenerationResults;
import org.ohdsi.webapi.cohortcharacterization.report.AnalysisItem;
import org.ohdsi.webapi.cohortcharacterization.report.AnalysisResultItem;
import org.ohdsi.webapi.cohortcharacterization.report.Report;
import org.ohdsi.webapi.cohortcharacterization.repository.AnalysisGenerationInfoEntityRepository;
import org.ohdsi.webapi.cohortcharacterization.repository.CcConceptSetRepository;
import org.ohdsi.webapi.cohortcharacterization.repository.CcGenerationEntityRepository;
import org.ohdsi.webapi.cohortcharacterization.repository.CcParamRepository;
import org.ohdsi.webapi.cohortcharacterization.repository.CcRepository;
import org.ohdsi.webapi.cohortcharacterization.repository.CcStrataRepository;
import org.ohdsi.webapi.cohortcharacterization.specification.CohortCharacterizationImpl;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.event.CohortDefinitionChangedEvent;
import org.ohdsi.webapi.common.DesignImportService;
import org.ohdsi.webapi.common.generation.AnalysisGenerationInfoEntity;
import org.ohdsi.webapi.common.generation.GenerationUtils;
import org.ohdsi.webapi.conceptset.ConceptSetExport;
import org.ohdsi.webapi.feanalysis.FeAnalysisService;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisCriteriaEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisWithCriteriaEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisWithStringEntity;
import org.ohdsi.webapi.feanalysis.event.FeAnalysisChangedEvent;
import org.ohdsi.webapi.job.GeneratesNotification;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.service.FeatureExtractionService;
import org.ohdsi.webapi.service.JobService;
import org.ohdsi.webapi.service.VocabularyService;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.annotations.CcGenerationId;
import org.ohdsi.webapi.shiro.annotations.DataSourceAccess;
import org.ohdsi.webapi.shiro.annotations.SourceKey;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceInfo;
import org.ohdsi.webapi.source.SourceService;
import org.ohdsi.webapi.sqlrender.SourceAwareSqlRender;
import org.ohdsi.webapi.tag.dto.TagNameListRequestDTO;
import org.ohdsi.webapi.util.CancelableJdbcTemplate;
import org.ohdsi.webapi.util.EntityUtils;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.ohdsi.webapi.util.ExportUtil;
import org.ohdsi.webapi.util.NameUtils;
import org.ohdsi.webapi.util.SessionUtils;
import org.ohdsi.webapi.util.SourceUtils;
import org.ohdsi.webapi.util.TempFileUtils;
import org.ohdsi.webapi.versioning.domain.CharacterizationVersion;
import org.ohdsi.webapi.versioning.domain.Version;
import org.ohdsi.webapi.versioning.domain.VersionBase;
import org.ohdsi.webapi.versioning.domain.VersionType;
import org.ohdsi.webapi.versioning.dto.VersionDTO;
import org.ohdsi.webapi.versioning.dto.VersionUpdateDTO;
import org.ohdsi.webapi.versioning.service.VersionService;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.EventListener;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.ohdsi.analysis.cohortcharacterization.design.CcResultType.DISTRIBUTION;
import static org.ohdsi.analysis.cohortcharacterization.design.CcResultType.PREVALENCE;
import static org.ohdsi.webapi.Constants.GENERATE_COHORT_CHARACTERIZATION;
import static org.ohdsi.webapi.Constants.Params.COHORT_CHARACTERIZATION_ID;
import static org.ohdsi.webapi.Constants.Params.JOB_AUTHOR;
import static org.ohdsi.webapi.Constants.Params.JOB_NAME;
import static org.ohdsi.webapi.Constants.Params.SOURCE_ID;
import org.ohdsi.webapi.security.PermissionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageImpl;

@Service
@Transactional
@DependsOn({"ccExportDTOToCcEntityConverter", "cohortDTOToCohortDefinitionConverter", "feAnalysisDTOToFeAnalysisConverter"})
public class CcServiceImpl extends AbstractDaoService implements CcService, GeneratesNotification {

    private static final String GENERATION_NOT_FOUND_ERROR = "generation cannot be found by id %d";
    private static final String[] PARAMETERS_RESULTS = {"cohort_characterization_generation_id", "threshold_level", "vocabulary_schema"};
    private static final String[] PARAMETERS_RESULTS_FILTERED = {"cohort_characterization_generation_id", "threshold_level",
            "analysis_ids", "cohort_ids", "vocabulary_schema"};
    private static final String[] PARAMETERS_COUNT = {"cohort_characterization_generation_id", "vocabulary_schema"};
    private static final String[] PREVALENCE_STATS_PARAMS = {"cdm_database_schema", "cdm_results_schema", "cc_generation_id", "analysis_id", "cohort_id", "covariate_id"};
    private final String QUERY_RESULTS = ResourceHelper.GetResourceAsString("/resources/cohortcharacterizations/sql/queryResults.sql");
    private final String QUERY_COUNT = ResourceHelper.GetResourceAsString("/resources/cohortcharacterizations/sql/queryCountWithoutThreshold.sql");
    private final String DELETE_RESULTS = ResourceHelper.GetResourceAsString("/resources/cohortcharacterizations/sql/deleteResults.sql");
    private final String DELETE_EXECUTION = ResourceHelper.GetResourceAsString("/resources/cohortcharacterizations/sql/deleteExecution.sql");
    private final String QUERY_PREVALENCE_STATS = ResourceHelper.GetResourceAsString("/resources/cohortcharacterizations/sql/queryCovariateStatsVocab.sql");

    private final String HYDRA_PACKAGE = "/resources/cohortcharacterizations/hydra/CohortCharacterization_v0.0.1.zip";

    private final static List<String> INCOMPLETE_STATUSES = ImmutableList.of(BatchStatus.STARTED, BatchStatus.STARTING, BatchStatus.STOPPING, BatchStatus.UNKNOWN)
            .stream().map(BatchStatus::name).collect(Collectors.toList());

    private Map<String, FeatureExtraction.PrespecAnalysis> prespecAnalysisMap = FeatureExtraction.getNameToPrespecAnalysis();

    private final EntityGraph defaultEntityGraph = EntityUtils.fromAttributePaths(
            "cohortDefinitions",
            "featureAnalyses",
            "stratas",
            "parameters",
            "createdBy",
            "modifiedBy"
    );

    private final List<String[]> executionPrevalenceHeaderLines = new ArrayList<String[]>() {{
        add(new String[]{"Analysis ID", "Analysis name", "Strata ID",
                "Strata name", "Cohort ID", "Cohort name", "Covariate ID", "Covariate name", "Covariate short name",
                "Count", "Percent"});
    }};

    private final List<String[]> executionDistributionHeaderLines = new ArrayList<String[]>() {{
        add(new String[]{"Analysis ID", "Analysis name", "Strata ID",
                "Strata name", "Cohort ID", "Cohort name", "Covariate ID", "Covariate name", "Covariate short name", "Value field","Missing Means Zero",
                "Count", "Avg", "StdDev", "Min", "P10", "P25", "Median", "P75", "P90", "Max"});
    }};

    private final List<String[]> executionComparativeHeaderLines = new ArrayList<String[]>() {{
        add(new String[]{"Analysis ID", "Analysis name", "Strata ID",
                "Strata name", "Target cohort ID", "Target cohort name", "Comparator cohort ID", "Comparator cohort name",
                "Covariate ID", "Covariate name", "Covariate short name", "Target count", "Target percent",
                "Comparator count", "Comparator percent", "Std. Diff Of Mean"});
    }};

    private final CcRepository repository;
    private final CcParamRepository paramRepository;
    private final CcStrataRepository strataRepository;
    private final CcConceptSetRepository conceptSetRepository;
    private final FeAnalysisService analysisService;
    private final CcGenerationEntityRepository ccGenerationRepository;
    private final FeatureExtractionService featureExtractionService;
    private final DesignImportService designImportService;
    private final AnalysisGenerationInfoEntityRepository analysisGenerationInfoEntityRepository;
    private final SourceService sourceService;
    private final GenerationUtils generationUtils;
    private final EntityManager entityManager;
    private final ApplicationEventPublisher eventPublisher;

    private final JobRepository jobRepository;
    private final SourceAwareSqlRender sourceAwareSqlRender;
    private final JobService jobService;
    private final JobInvalidator jobInvalidator;
    private final GenericConversionService genericConversionService;
    private final VocabularyService vocabularyService;
    private VersionService<CharacterizationVersion> versionService;
    
    private PermissionService permissionService;

    @Value("${security.defaultGlobalReadPermissions}")
    private boolean defaultGlobalReadPermissions;
    
    private final Environment env;

    public CcServiceImpl(
            final CcRepository ccRepository,
            final CcParamRepository paramRepository,
            final CcStrataRepository strataRepository,
            final CcConceptSetRepository conceptSetRepository,
            final FeAnalysisService analysisService,
            final CcGenerationEntityRepository ccGenerationRepository,
            final FeatureExtractionService featureExtractionService,
            final ConversionService conversionService,
            final DesignImportService designImportService,
            final JobRepository jobRepository,
            final AnalysisGenerationInfoEntityRepository analysisGenerationInfoEntityRepository,
            final SourceService sourceService,
            final GenerationUtils generationUtils,
            final SourceAwareSqlRender sourceAwareSqlRender,
            final EntityManager entityManager,
            final JobService jobService,
            final ApplicationEventPublisher eventPublisher,
            final JobInvalidator jobInvalidator,
            final VocabularyService vocabularyService,
            final VersionService<CharacterizationVersion> versionService,
            final PermissionService permissionService,
            @Qualifier("conversionService") final GenericConversionService genericConversionService,
            Environment env) {
        this.repository = ccRepository;
        this.paramRepository = paramRepository;
        this.strataRepository = strataRepository;
        this.conceptSetRepository = conceptSetRepository;
        this.analysisService = analysisService;
        this.ccGenerationRepository = ccGenerationRepository;
        this.featureExtractionService = featureExtractionService;
        this.designImportService = designImportService;
        this.jobRepository = jobRepository;
        this.analysisGenerationInfoEntityRepository = analysisGenerationInfoEntityRepository;
        this.sourceService = sourceService;
        this.generationUtils = generationUtils;
        this.sourceAwareSqlRender = sourceAwareSqlRender;
        this.entityManager = entityManager;
        this.jobService = jobService;
        this.eventPublisher = eventPublisher;
        this.jobInvalidator = jobInvalidator;
        this.vocabularyService = vocabularyService;
        this.permissionService = permissionService;
        this.genericConversionService = genericConversionService;
        this.versionService = versionService;
        this.env = env;
        SerializedCcToCcConverter.setConversionService(conversionService);
    }

    @Override
    public CohortCharacterizationEntity createCc(final CohortCharacterizationEntity entity) {
        entity.setCreatedBy(getCurrentUser());
        entity.setCreatedDate(new Date());
        return saveCc(entity);
    }

    private CohortCharacterizationEntity saveCc(final CohortCharacterizationEntity entity) {
        CohortCharacterizationEntity savedEntity = repository.saveAndFlush(entity);

        for(CcStrataEntity strata: entity.getStratas()){
          strata.setCohortCharacterization(savedEntity);
          strataRepository.save(strata);
        }

        for(CcParamEntity param: entity.getParameters()){
          param.setCohortCharacterization(savedEntity);
          paramRepository.save(param);
        }

        entityManager.flush();
        entityManager.refresh(savedEntity);

        savedEntity = findByIdWithLinkedEntities(savedEntity.getId());

        Date modifiedDate = savedEntity.getModifiedDate();
        savedEntity.setModifiedDate(null);
        final String serialized = this.serializeCc(savedEntity);
        savedEntity.setHashCode(serialized.hashCode());
        savedEntity.setModifiedDate(modifiedDate);

        return repository.save(savedEntity);
    }

    @EventListener
    @Transactional
    @Override
    public void onCohortDefinitionChanged(CohortDefinitionChangedEvent event) {

        List<CohortCharacterizationEntity> ccList = repository.findByCohortDefinition(event.getCohortDefinition());
        ccList.forEach(this::saveCc);
    }

    @EventListener
    @Transactional
    public void onFeAnalysisChanged(FeAnalysisChangedEvent event) {

        List<CohortCharacterizationEntity> ccList = repository.findByFeatureAnalysis(event.getFeAnalysis());
        ccList.forEach(this::saveCc);
    }

    @Override
    @Transactional
    public void assignTag(Long id, int tagId) {
        CohortCharacterizationEntity entity = findById(id);
        checkOwnerOrAdminOrGranted(entity);
        assignTag(entity, tagId);
    }

    @Override
    @Transactional
    public void unassignTag(Long id, int tagId) {
        CohortCharacterizationEntity entity = findById(id);
        checkOwnerOrAdminOrGranted(entity);
        unassignTag(entity, tagId);
    }

    @Override
    public int getCountCcWithSameName(Long id, String name) {
        return repository.getCountCcWithSameName(id, name);
    }

    @Override
    public void deleteCc(Long ccId) {
        repository.delete(ccId);
    }

    @Override
    public CohortCharacterizationEntity updateCc(final CohortCharacterizationEntity entity) {
        final CohortCharacterizationEntity foundEntity = repository.findById(entity.getId())
                .orElseThrow(() -> new NotFoundException("CC entity isn't found"));

        updateLinkedFields(entity, foundEntity);

        if (StringUtils.isNotEmpty(entity.getName())) {
            foundEntity.setName(entity.getName());
        }
        foundEntity.setDescription(entity.getDescription());
        foundEntity.setStratifiedBy(entity.getStratifiedBy());
        if (Objects.nonNull(entity.getStrataOnly())) {
          foundEntity.setStrataOnly(entity.getStrataOnly());
        }

        foundEntity.setModifiedDate(new Date());
        foundEntity.setModifiedBy(getCurrentUser());

        return saveCc(foundEntity);
    }

    private void updateLinkedFields(final CohortCharacterizationEntity entity, final CohortCharacterizationEntity foundEntity) {
        updateConceptSet(entity, foundEntity);
        updateParams(entity, foundEntity);
        updateAnalyses(entity, foundEntity);
        updateCohorts(entity, foundEntity);
        updateStratas(entity, foundEntity);
    }

  private void updateConceptSet(CohortCharacterizationEntity entity, CohortCharacterizationEntity foundEntity) {
      if (Objects.nonNull(foundEntity.getConceptSetEntity()) && Objects.nonNull(entity.getConceptSetEntity())) {
        foundEntity.getConceptSetEntity().setRawExpression(entity.getConceptSetEntity().getRawExpression());
      } else if (Objects.nonNull(entity.getConceptSetEntity())) {
        CcStrataConceptSetEntity cse = new CcStrataConceptSetEntity();
        cse.setCohortCharacterization(foundEntity);
        cse.setRawExpression(entity.getConceptSetEntity().getRawExpression());
        foundEntity.setConceptSetEntity(cse);
      } else {
        foundEntity.setConceptSetEntity(null);
      }
  }

  private void updateStratas(CohortCharacterizationEntity entity, CohortCharacterizationEntity foundEntity) {
        final List<CcStrataEntity> stratasToDelete = getLinksToDelete(foundEntity,
                existingLink -> entity.getStratas().stream().noneMatch(newLink -> Objects.equals(newLink.getId(), existingLink.getId())),
                CohortCharacterizationEntity::getStratas);
        foundEntity.getStratas().removeAll(stratasToDelete);
        strataRepository.delete(stratasToDelete);
        Map<Long, CcStrataEntity> strataEntityMap = foundEntity.getStratas().stream()
                .collect(Collectors.toMap(CcStrataEntity::getId, s -> s));

        List<CcStrataEntity> updatedStratas = entity.getStratas().stream().map(updated -> {
            updated.setCohortCharacterization(foundEntity);
            if (Objects.nonNull(updated.getId())) {
                CcStrataEntity strata = strataEntityMap.get(updated.getId());
                // strata will be null in case of importing new characterization
                if (strata == null) {
                    return updated;
                }
                if (StringUtils.isNotBlank(updated.getName())) {
                    strata.setName(updated.getName());
                }
                strata.setExpressionString(updated.getExpressionString());
                return strata;
            } else {
                return updated;
            }
        }).collect(Collectors.toList());
        entity.setStratas(new HashSet<>(strataRepository.save(updatedStratas)));
    }

    private void updateCohorts(final CohortCharacterizationEntity entity, final CohortCharacterizationEntity foundEntity) {
        foundEntity.getCohortDefinitions().clear();
        foundEntity.getCohortDefinitions().addAll(entity.getCohortDefinitions());
    }

    private void updateAnalyses(final CohortCharacterizationEntity entity, final CohortCharacterizationEntity foundEntity) {
        foundEntity.getFeatureAnalyses().clear();
        foundEntity.getFeatureAnalyses().addAll(entity.getFeatureAnalyses());
    }

    private <T extends WithId> List<T> getLinksToDelete(final CohortCharacterizationEntity foundEntity,
                                                        Predicate<? super T> filterPredicate,
                                                        Function<CohortCharacterizationEntity, Set<T>> getter) {
        return getter.apply(foundEntity)
                .stream()
                .filter(filterPredicate)
                .collect(Collectors.toList());
    }

    private void updateParams(final CohortCharacterizationEntity entity, final CohortCharacterizationEntity foundEntity) {
        updateOrCreateParams(entity, foundEntity);
        deleteParams(entity, foundEntity);
    }

    private void deleteParams(final CohortCharacterizationEntity entity, final CohortCharacterizationEntity foundEntity) {
        final Map<String, CcParamEntity> nameToParamFromInputMap = buildParamNameToParamMap(entity);
        List<CcParamEntity> paramsForDelete  = getLinksToDelete(foundEntity,
                parameter -> !nameToParamFromInputMap.containsKey(parameter.getName()),
                CohortCharacterizationEntity::getParameters);
        foundEntity.getParameters().removeAll(paramsForDelete);
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
        updateStratas(entity, persistedCohortCharacterization);
        updateConceptSet(entity, persistedCohortCharacterization);
        
        importCohorts(entity, persistedCohortCharacterization);
        List<Integer> savedAnalysesIds = importAnalyses(entity, persistedCohortCharacterization);

        final CohortCharacterizationEntity savedEntity = saveCc(persistedCohortCharacterization);

        eventPublisher.publishEvent(new CcImportEvent(savedAnalysesIds));

        return savedEntity;
    }

    @Override
    public String getNameForCopy(String dtoName) {
        return NameUtils.getNameForCopy(dtoName, this::getNamesLike, repository.findByName(dtoName));
    }

    @Override
    public String getNameWithSuffix(String dtoName) {
        return NameUtils.getNameWithSuffix(dtoName, this::getNamesLike);
    }

    @Override
    public String serializeCc(final Long id) {

        return Utils.serialize(exportCc(id), true);
    }

    @Override
    public String serializeCc(final CohortCharacterizationEntity cohortCharacterizationEntity) {
        return new SerializedCcToCcConverter().convertToDatabaseColumn(cohortCharacterizationEntity);
    }

    private CohortCharacterizationImpl exportCc(final Long id) {
      final CohortCharacterizationEntity cohortCharacterizationEntity = repository.findById(id)
              .orElseThrow(() -> new IllegalArgumentException("Cohort characterization cannot be found by id: " + id));
      CohortCharacterizationImpl cc = genericConversionService.convert(cohortCharacterizationEntity, CohortCharacterizationImpl.class);
      ExportUtil.clearCreateAndUpdateInfo(cc);
      cc.getFeatureAnalyses().forEach(ExportUtil::clearCreateAndUpdateInfo);
      cc.getCohorts().forEach(ExportUtil::clearCreateAndUpdateInfo);
      cc.setOrganizationName(env.getRequiredProperty("organization.name"));
      return cc;
    }

    @Override
    public CohortCharacterizationEntity findById(final Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cohort characterization with id: " + id + " cannot be found"));
    }

    @Override
    public CohortCharacterizationEntity findByIdWithLinkedEntities(final Long id) {
        return repository.findOne(id, defaultEntityGraph);
    }

    @Override
    @DataSourceAccess
    public CohortCharacterization findDesignByGenerationId(@CcGenerationId final Long id) {
        final AnalysisGenerationInfoEntity entity = analysisGenerationInfoEntityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Analysis with id: " + id + " cannot be found"));
        return genericConversionService.convert(Utils.deserialize(entity.getDesign(),
                new TypeReference<CcExportDTO>() {}), CohortCharacterizationEntity.class);
    }

    @Override
    public Page<CohortCharacterizationEntity> getPageWithLinkedEntities(final Pageable pageable) {
      return repository.findAll(pageable, defaultEntityGraph);
      
    }

    @Override
    public Page<CohortCharacterizationEntity> getPage(final Pageable pageable) {
      List<CohortCharacterizationEntity> ccList = repository.findAll()
              .stream().filter(!defaultGlobalReadPermissions ? entity -> permissionService.hasReadAccess(entity) : entity -> true)
              .collect(Collectors.toList());
      return getPageFromResults(pageable, ccList);
    }
    
    private Page<CohortCharacterizationEntity> getPageFromResults(Pageable pageable, List<CohortCharacterizationEntity> results) {
      // Calculate the start and end indices for the current page
      int startIndex = pageable.getPageNumber() * pageable.getPageSize();
      int endIndex = Math.min(startIndex + pageable.getPageSize(), results.size());      
      
      return new PageImpl<>(results.subList(startIndex, endIndex), pageable, results.size());
    }

    @Override
    public void hydrateAnalysis(Long analysisId, String packageName, OutputStream out) {

      if (packageName == null || !Utils.isAlphaNumeric(packageName)) {
        throw new IllegalArgumentException("The package name must be alphanumeric only.");
      }
      CohortCharacterizationImpl analysis = exportCc(analysisId);
      analysis.setPackageName(packageName);
      String studySpecs = Utils.serialize(analysis, true);
      Hydra hydra = new Hydra(studySpecs);
      File skeletonFile = null;
      try {
        skeletonFile = TempFileUtils.copyResourceToTempFile(HYDRA_PACKAGE, "cc-", ".zip");
        hydra.setExternalSkeletonFileName(skeletonFile.getAbsolutePath());
        hydra.hydrate(out);
      } catch (IOException e) {
        log.error("Failed to hydrate cohort characterization", e);
        throw new InternalServerErrorException(e);
      } finally {
        FileUtils.deleteQuietly(skeletonFile);
      }
    }

    @Override
    @DataSourceAccess
    public JobExecutionResource generateCc(final Long id, @SourceKey final String sourceKey) {

        CcService ccService = this;
        Source source = getSourceRepository().findBySourceKey(sourceKey);

        JobParametersBuilder builder = new JobParametersBuilder();

        builder.addString(JOB_NAME, String.format("Generating cohort characterization %d : %s (%s)", id, source.getSourceName(), source.getSourceKey()));
        builder.addString(COHORT_CHARACTERIZATION_ID, String.valueOf(id));
        builder.addString(SOURCE_ID, String.valueOf(source.getSourceId()));
        builder.addString(JOB_AUTHOR, getCurrentUserLogin());

        CancelableJdbcTemplate jdbcTemplate = getSourceJdbcTemplate(source);

        SimpleJobBuilder generateCohortJob = generationUtils.buildJobForCohortBasedAnalysisTasklet(
                GENERATE_COHORT_CHARACTERIZATION,
                source,
                builder,
                jdbcTemplate,
                chunkContext -> {
                    Long ccId = Long.valueOf(chunkContext.getStepContext().getJobParameters().get(COHORT_CHARACTERIZATION_ID).toString());
                    return ccService.findById(ccId).getCohortDefinitions();
                },
                new GenerateCohortCharacterizationTasklet(
                        jdbcTemplate,
                        getTransactionTemplate(),
                        ccService,
                        analysisGenerationInfoEntityRepository,
                        sourceService,
                        userRepository
                )
        );

        final JobParameters jobParameters = builder.toJobParameters();

        return jobService.runJob(generateCohortJob.build(), jobParameters);
    }

    @Override
    public List<CcGenerationEntity> findGenerationsByCcId(final Long id) {
        return ccGenerationRepository.findByCohortCharacterizationIdOrderByIdDesc(id, EntityUtils.fromAttributePaths("source"));
    }

    @Override
    public CcGenerationEntity findGenerationById(final Long id) {
        return ccGenerationRepository.findById(id, EntityUtils.fromAttributePaths("source"));
    }

    @Override
    public List<CcGenerationEntity> findGenerationsByCcIdAndSource(final Long id, final String sourceKey) {
        return ccGenerationRepository.findByCohortCharacterizationIdAndSourceSourceKeyOrderByIdDesc(id, sourceKey, EntityUtils.fromAttributePaths("source"));
    }

    public List<CcGenerationEntity> findAllIncompleteGenerations() {
        return ccGenerationRepository.findByStatusIn(INCOMPLETE_STATUSES);
    }

    protected List<CcResult> findResults(final Long generationId, ExecutionResultRequest params) {
        final CcGenerationEntity generationEntity = ccGenerationRepository.findById(generationId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(GENERATION_NOT_FOUND_ERROR, generationId)));
        final Source source = generationEntity.getSource();
        String analysis = params.getAnalysisIds().stream().map(String::valueOf).collect(Collectors.joining(","));
        String cohorts = params.getCohortIds().stream().map(String::valueOf).collect(Collectors.joining(","));
        String generationResults = sourceAwareSqlRender.renderSql(source.getSourceId(), QUERY_RESULTS, PARAMETERS_RESULTS_FILTERED,
                new String[]{String.valueOf(generationId), String.valueOf(params.getThresholdValuePct()),
                        analysis, cohorts, SourceUtils.getVocabularyQualifier(source)});
        final String tempSchema = SourceUtils.getTempQualifier(source);
        String translatedSql = SqlTranslate.translateSql(generationResults, source.getSourceDialect(), SessionUtils.sessionId(), tempSchema);
        return getGenerationResults(source, translatedSql);
    }

    @Override
    @DataSourceAccess
    public Long getCCResultsTotalCount(@CcGenerationId final Long generationId) {
        final CcGenerationEntity generationEntity = ccGenerationRepository.findById(generationId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(GENERATION_NOT_FOUND_ERROR, generationId)));
        final Source source = generationEntity.getSource();
        String countReq = sourceAwareSqlRender.renderSql(source.getSourceId(), QUERY_COUNT, PARAMETERS_COUNT,
                new String[]{String.valueOf(generationId), SourceUtils.getVocabularyQualifier(source)});
        final String tempSchema = SourceUtils.getTempQualifier(source);
        String translatedSql = SqlTranslate.translateSql(countReq, source.getSourceDialect(), SessionUtils.sessionId(), tempSchema);
        return this.getSourceJdbcTemplate(source).queryForObject(translatedSql, Long.class);
    }

    @Override
    @DataSourceAccess
    public GenerationResults exportExecutionResult(@CcGenerationId final Long generationId, ExportExecutionResultRequest params) {
        GenerationResults res = findResult(generationId, params);

        if (params.isFilterUsed()) {
            res.setReports(res.getReports().stream()
                    .filter(r -> params.isComparative() == null || params.isComparative() == r.isComparative)
                    .filter(r -> params.isSummary() == null || params.isSummary() == r.isSummary)
                    .collect(Collectors.toList()));
        }

        return res;
    }

    @Override
    @DataSourceAccess
    public GenerationResults findData(@CcGenerationId final Long generationId, ExecutionResultRequest params) {
        if (params.getShowEmptyResults()) {
          params.setThresholdValuePct(Constants.DEFAULT_THRESHOLD); //Don't cut threshold results when all results requested
        }
        GenerationResults res = findResult(generationId, params);
        boolean hasComparativeReports = res.getReports().stream()
                .anyMatch(report -> report.isComparative);
        if (hasComparativeReports) {
            // if there're comparative reports - return only them as simple reports won't be shown on ui
            res.setReports(res.getReports().stream()
                    .filter(report -> report.isComparative)
                    .collect(Collectors.toList()));
        }
        res.setPrevalenceThreshold(params.getThresholdValuePct());
        return res;
    }
    
    @Override
    @DataSourceAccess
    public List<CcResult> findResultAsList(@CcGenerationId final Long generationId, float thresholdLevel) {
        ExecutionResultRequest params = new ExecutionResultRequest();
        CcGenerationEntity generationEntity = ccGenerationRepository.findById(generationId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(GENERATION_NOT_FOUND_ERROR, generationId)));
        CohortCharacterizationEntity characterization = generationEntity.getCohortCharacterization();
        params.setThresholdValuePct(thresholdLevel);
        params.setCohortIds(characterization.getCohortDefinitions().stream()
                .map(CohortDefinition::getId).collect(Collectors.toList()));
        params.setAnalysisIds(characterization.getFeatureAnalyses().stream()
                .map(this::mapFeatureAnalysisId).collect(Collectors.toList()));
        params.setDomainIds(generationEntity.getCohortCharacterization().getFeatureAnalyses().stream()
                .map(fa -> fa.getDomain().toString()).distinct().collect(Collectors.toList()));
        return findResults(generationId, params);
    }
    
    @Override
    @DataSourceAccess
    public GenerationResults findResult(@CcGenerationId final Long generationId, ExecutionResultRequest params) {
        CcGenerationEntity generationEntity = ccGenerationRepository.findById(generationId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(GENERATION_NOT_FOUND_ERROR, generationId)));

        CohortCharacterizationEntity characterization = generationEntity.getCohortCharacterization();
        Set<CohortDefinition> cohortDefs = characterization.getCohorts();
        Set<FeAnalysisEntity> featureAnalyses = characterization.getFeatureAnalyses();

        // if filter is not used then it must be initialized first
        if (!params.isFilterUsed()) {
            params.setCohortIds(characterization.getCohortDefinitions().stream()
                    .map(CohortDefinition::getId).collect(Collectors.toList()));
            params.setAnalysisIds(featureAnalyses.stream().map(this::mapFeatureAnalysisId).collect(Collectors.toList()));
            params.setDomainIds(generationEntity.getCohortCharacterization().getFeatureAnalyses().stream()
                    .map(fa -> fa.getDomain().toString()).distinct().collect(Collectors.toList()));
        } else {
            List<Integer> analysisIds = params.getAnalysisIds().stream().map(analysisId -> {
              FeAnalysisEntity fe = featureAnalyses.stream()
                    .filter(fa -> Objects.equals(fa.getId(), analysisId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Feature with id=%s not found in analysis", analysisId)));
              return mapFeatureAnalysisId(fe);
            }).collect(Collectors.toList());
            params.setAnalysisIds(analysisIds);
        }
        // remove domains which cannot be used as corresponding analyses are not selected
        params.getDomainIds().removeIf(s ->
                featureAnalyses.stream()
                        .noneMatch(fe -> fe.getDomain().toString().equals(s) && params.getAnalysisIds().contains(mapFeatureAnalysisId(fe))));
        // remove analyses which cannot be used as corresponding domains are not selected
        params.getAnalysisIds().removeIf(s ->
                featureAnalyses.stream()
                        .noneMatch(fe -> mapFeatureAnalysisId(fe).equals(s) && params.getDomainIds().contains(fe.getDomain().toString())));

        List<CcResult> ccResults = findResults(generationId, params);

        // create initial structure and fill with results
        Map<Integer, AnalysisItem> analysisMap = new HashMap<>();
        ccResults
                .stream()
                .peek(cc -> {
                  if (StandardFeatureAnalysisType.PRESET.toString().equals(cc.getFaType())) {
                    featureAnalyses.stream()
                            .filter(fa -> Objects.equals(fa.getDesign(), cc.getAnalysisName()))
                            .findFirst()
                            .ifPresent(v -> cc.setAnalysisId(v.getId()));
                  }
                })
                .forEach(ccResult -> {
                    if (ccResult instanceof CcPrevalenceStat) {
                        analysisMap.putIfAbsent(ccResult.getAnalysisId(), new AnalysisItem());
                        AnalysisItem analysisItem = analysisMap.get(ccResult.getAnalysisId());
                        analysisItem.setType(ccResult.getResultType());
                        analysisItem.setName(ccResult.getAnalysisName());
                        analysisItem.setFaType(ccResult.getFaType());
                        List<CcResult> results = analysisItem.getOrCreateCovariateItem(
                                ((CcPrevalenceStat) ccResult).getCovariateId(), ccResult.getStrataId());
                        results.add(ccResult);
                    }
                });

        cohortDefs = cohortDefs
                .stream()
                .filter(def -> params.getCohortIds().contains(def.getId()))
                .collect(Collectors.toSet());

        List<Report> reports = prepareReportData(analysisMap, cohortDefs, featureAnalyses);

        GenerationResults res = new GenerationResults();
        res.setReports(reports);
        res.setCount(ccResults.size());
        return res;
    }

    private Integer mapFeatureAnalysisId(FeAnalysisEntity feAnalysis) {

      if (feAnalysis.isPreset()) {
         return prespecAnalysisMap.values().stream().filter(p -> Objects.equals(p.analysisName, feAnalysis.getDesign()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Preset analysis with id=%s does not exist", feAnalysis.getId())))
                .analysisId;
      } else {
        return feAnalysis.getId();
      }
    }

    private String mapFeatureName(FeAnalysisEntity entity) {

      if (StandardFeatureAnalysisType.PRESET == entity.getType()) {
        return entity.getDesign().toString();
      }
      return entity.getName();
    }

    private List<Report> prepareReportData(Map<Integer, AnalysisItem> analysisMap, Set<CohortDefinition> cohortDefs,
                                           Set<FeAnalysisEntity> featureAnalyses) {
        // Create map to get cohort name by its id
        final Map<Integer, CohortDefinition> definitionMap = cohortDefs.stream()
                .collect(Collectors.toMap(CohortDefinition::getId, Function.identity()));
        // Create map to get feature analyses by its name
        final Map<String, String> feAnalysisMap = featureAnalyses.stream()
                .collect(Collectors.toMap(this::mapFeatureName, entity -> entity.getDomain().toString()));

        List<Report> reports = new ArrayList<>();
        try {
            // list to accumulate results from simple reports
            List<AnalysisResultItem> simpleResultSummary = new ArrayList<>();
            // list to accumulate results from comparative reports
            List<AnalysisResultItem> comparativeResultSummary = new ArrayList<>();
            // do not create summary reports when only one analyses is present
            boolean ignoreSummary = analysisMap.keySet().size() == 1;
            for (Integer analysisId : analysisMap.keySet()) {
                analysisMap.putIfAbsent(analysisId, new AnalysisItem());
                AnalysisItem analysisItem = analysisMap.get(analysisId);
                AnalysisResultItem resultItem = analysisItem.getSimpleItems(definitionMap, feAnalysisMap);
                Report simpleReport = new Report(analysisItem.getName(), analysisId, resultItem);
                simpleReport.faType = analysisItem.getFaType();
                simpleReport.domainId = feAnalysisMap.get(analysisItem.getName());

                if (PREVALENCE.equals(analysisItem.getType())) {
                    simpleReport.header = executionPrevalenceHeaderLines;
                    simpleReport.resultType = PREVALENCE;
                    // Summary comparative reports are only available for prevalence type
                    simpleResultSummary.add(resultItem);
                } else if (DISTRIBUTION.equals(analysisItem.getType())) {
                    simpleReport.header = executionDistributionHeaderLines;
                    simpleReport.resultType = DISTRIBUTION;
                }
                reports.add(simpleReport);

                // comparative mode
                if (definitionMap.size() == 2) {
                    Iterator<CohortDefinition> iter = definitionMap.values().iterator();
                    CohortDefinition firstCohortDef = iter.next();
                    CohortDefinition secondCohortDef = iter.next();
                    AnalysisResultItem comparativeResultItem = analysisItem.getComparativeItems(firstCohortDef,
                            secondCohortDef, feAnalysisMap);
                    Report comparativeReport = new Report(analysisItem.getName(), analysisId, comparativeResultItem);
                    comparativeReport.header = executionComparativeHeaderLines;
                    comparativeReport.isComparative = true;
                    comparativeReport.faType = analysisItem.getFaType();
                    comparativeReport.domainId = feAnalysisMap.get(analysisItem.getName());
                    if (PREVALENCE.equals(analysisItem.getType())) {
                        comparativeReport.resultType = PREVALENCE;
                        // Summary comparative reports are only available for prevalence type
                        comparativeResultSummary.add(comparativeResultItem);
                    } else if (DISTRIBUTION.equals(analysisItem.getType())) {
                        comparativeReport.resultType = DISTRIBUTION;
                    }
                    reports.add(comparativeReport);
                }
            }
            if (!ignoreSummary) {
                // summary comparative reports are only available for prevalence type
                if (!simpleResultSummary.isEmpty()) {
                    Report simpleSummaryData = new Report("All prevalence covariates", simpleResultSummary);
                    simpleSummaryData.header = executionPrevalenceHeaderLines;
                    simpleSummaryData.isSummary = true;
                    simpleSummaryData.resultType = PREVALENCE;
                    reports.add(simpleSummaryData);
                }
                // comparative mode
                if (!comparativeResultSummary.isEmpty()) {
                    Report comparativeSummaryData = new Report("All prevalence covariates", comparativeResultSummary);
                    comparativeSummaryData.header = executionComparativeHeaderLines;
                    comparativeSummaryData.isSummary = true;
                    comparativeSummaryData.isComparative = true;
                    comparativeSummaryData.resultType = PREVALENCE;
                    reports.add(comparativeSummaryData);
                }
            }

            return reports;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<CcPrevalenceStat> getPrevalenceStatsByGenerationId(Long id, Long analysisId, Long cohortId, Long covariateId) {
        final CcGenerationEntity generationEntity = ccGenerationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(String.format(GENERATION_NOT_FOUND_ERROR, id)));
        final Source source = generationEntity.getSource();
        final String cdmSchema = SourceUtils.getCdmQualifier(source);
        final String resultSchema = SourceUtils.getResultsQualifier(source);
        final String tempSchema = SourceUtils.getTempQualifier(source);
        String prevalenceStats = sourceAwareSqlRender.renderSql(source.getSourceId(), QUERY_PREVALENCE_STATS, PREVALENCE_STATS_PARAMS,
                new String[]{ cdmSchema, resultSchema, String.valueOf(id), String.valueOf(analysisId), String.valueOf(cohortId), String.valueOf(covariateId) });
        String translatedSql = SqlTranslate.translateSql(prevalenceStats, source.getSourceDialect(), SessionUtils.sessionId(), tempSchema);
        String[] stmts = SqlSplit.splitSql(translatedSql);
        if (stmts.length == 1) { // Some DBMS like HIVE fails when a single statement ends with dot-comma
            translatedSql = StringUtils.removeEnd(translatedSql.trim(), ";");
        }
        return getSourceJdbcTemplate(source).query(translatedSql, (rs, rowNum) -> {
            CcPrevalenceStat stat = new CcPrevalenceStat();
            stat.setAvg(rs.getDouble("stat_value"));
            stat.setConceptId(rs.getLong("concept_id"));
            stat.setConceptName(rs.getString("concept_name"));
            stat.setCount(rs.getLong("count_value"));
            stat.setCovariateId(rs.getLong("covariate_id"));
            stat.setCovariateName(rs.getString("covariate_name"));
            stat.setAnalysisId(rs.getInt("analysis_id"));
            stat.setAnalysisName(rs.getString("analysis_name"));
            stat.setSourceKey(source.getSourceKey());
            stat.setDistance(rs.getInt("min_levels_of_separation"));
            stat.setStrataId(rs.getLong("strata_id"));
            stat.setStrataName(rs.getString("strata_name"));
            stat.setFaType(rs.getString("fa_type"));
            return stat;
        });
    }

    @Override
    @DataSourceAccess
    public void deleteCcGeneration(@CcGenerationId Long generationId) {
        final CcGenerationEntity generationEntity = ccGenerationRepository.findById(generationId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(GENERATION_NOT_FOUND_ERROR, generationId)));
        final Source source = generationEntity.getSource();
        final String sql = sourceAwareSqlRender.renderSql(source.getSourceId(), DELETE_RESULTS, PARAMETERS_RESULTS, new String[]{ String.valueOf(generationId) });
        final String tempSchema = SourceUtils.getTempQualifier(source);
        final String translatedSql = SqlTranslate.translateSql(sql, source.getSourceDialect(), SessionUtils.sessionId(), tempSchema);
        getSourceJdbcTemplate(source).execute(translatedSql);

        final String deleteJobSql = sourceAwareSqlRender.renderSql(source.getSourceId(), DELETE_EXECUTION,
                new String[]{ "ohdsiSchema", "execution_id" },
                new String[]{ getOhdsiSchema(), String.valueOf(generationId) }
        );
        final String translatedJobSql = SqlTranslate.translateSql(deleteJobSql, getDialect());
        getJdbcTemplate().batchUpdate(translatedJobSql.split(";"));
    }

    @Override
    @DataSourceAccess
    public void cancelGeneration(Long id, @SourceKey String sourceKey) {

        Source source = getSourceRepository().findBySourceKey(sourceKey);
        if (Objects.isNull(source)) {
            throw new NotFoundException();
        }
        jobService.cancelJobExecution(j -> {
            JobParameters jobParameters = j.getJobParameters();
            String jobName = j.getJobInstance().getJobName();
            return Objects.equals(jobParameters.getString(SOURCE_ID), Integer.toString(source.getSourceId()))
                    && Objects.equals(jobParameters.getString(COHORT_CHARACTERIZATION_ID), Long.toString(id))
                    && Objects.equals(getJobName(), jobName);
        });
    }

    public List<VersionDTO> getVersions(final long id) {
        List<VersionBase> versions = versionService.getVersions(VersionType.CHARACTERIZATION, id);
        return versions.stream()
                .map(v -> genericConversionService.convert(v, VersionDTO.class))
                .collect(Collectors.toList());
    }

    public CcVersionFullDTO getVersion(final long id, final int version) {
        checkVersion(id, version, false);
        CharacterizationVersion characterizationVersion = versionService.getById(VersionType.CHARACTERIZATION, id, version);

        return genericConversionService.convert(characterizationVersion, CcVersionFullDTO.class);
    }

    public VersionDTO updateVersion(final long id, final int version,
                                    VersionUpdateDTO updateDTO) {
        checkVersion(id, version);
        updateDTO.setAssetId(id);
        updateDTO.setVersion(version);
        CharacterizationVersion updated = versionService.update(VersionType.CHARACTERIZATION, updateDTO);

        return genericConversionService.convert(updated, VersionDTO.class);
    }

    public void deleteVersion(final long id, final int version) {
        checkVersion(id, version);
        versionService.delete(VersionType.CHARACTERIZATION, id, version);
    }

    public CohortCharacterizationDTO copyAssetFromVersion(final long id, final int version) {
        checkVersion(id, version, false);
        CharacterizationVersion characterizationVersion = versionService.getById(VersionType.CHARACTERIZATION, id, version);

        CcVersionFullDTO fullDTO = genericConversionService.convert(characterizationVersion, CcVersionFullDTO.class);
        CohortCharacterizationEntity entity =
                genericConversionService.convert(fullDTO.getEntityDTO(), CohortCharacterizationEntity.class);
        entity.setId(null);
        entity.setTags(null);
        entity.setName(NameUtils.getNameForCopy(entity.getName(), this::getNamesLike, repository.findByName(entity.getName())));

        CohortCharacterizationEntity saved = createCc(entity);
        return genericConversionService.convert(saved, CohortCharacterizationDTO.class);
    }

    private void checkVersion(long id, int version) {
        checkVersion(id, version, true);
    }

    private void checkVersion(long id, int version, boolean checkOwnerShip) {
        Version characterizationVersion = versionService.getById(VersionType.CHARACTERIZATION, id, version);
        ExceptionUtils.throwNotFoundExceptionIfNull(characterizationVersion,
                String.format("There is no cohort characterization version with id = %d.", version));

        CohortCharacterizationEntity entity = findById(id);
        if (checkOwnerShip) {
            checkOwnerOrAdminOrGranted(entity);
        }
    }

    public CharacterizationVersion saveVersion(long id) {
        CohortCharacterizationEntity def = findById(id);
        CharacterizationVersion version = genericConversionService.convert(def, CharacterizationVersion.class);

        UserEntity user = Objects.nonNull(def.getModifiedBy()) ? def.getModifiedBy() : def.getCreatedBy();
        Date versionDate = Objects.nonNull(def.getModifiedDate()) ? def.getModifiedDate() : def.getCreatedDate();
        version.setCreatedBy(user);
        version.setCreatedDate(versionDate);
        return versionService.create(VersionType.CHARACTERIZATION, version);
    }

    private List<String> getNamesLike(String copyName) {

      return repository.findAllByNameStartsWith(copyName).stream().map(CohortCharacterizationEntity::getName).collect(Collectors.toList());
    }

    @Override
    public String getJobName() {
        return GENERATE_COHORT_CHARACTERIZATION;
    }

    @Override
    public String getExecutionFoldingKey() {
        return COHORT_CHARACTERIZATION_ID;
    }

    @Override
    public List<ConceptSetExport> exportConceptSets(CohortCharacterization cohortCharacterization) {

        SourceInfo prioritySource = new SourceInfo(vocabularyService.getPriorityVocabularySource());
        return cohortCharacterization.getStrataConceptSets().stream()
                .map(cs -> vocabularyService.exportConceptSet(cs, prioritySource))
                .collect(Collectors.toList());
    }

    @Override
    public List<CcShortDTO> listByTags(TagNameListRequestDTO requestDTO) {
        List<String> names = requestDTO.getNames().stream()
                .map(name -> name.toLowerCase(Locale.ROOT))
                .collect(Collectors.toList());
        List<CohortCharacterizationEntity> entities = repository.findByTags(names);
        return listByTags(entities, names, CcShortDTO.class);
    }

    private List<CcResult> getGenerationResults(final Source source, final String translatedSql) {
        return this.getSourceJdbcTemplate(source).query(translatedSql, (rs, rowNum) -> {
            final String type = rs.getString("type");
            if (StringUtils.equals(type, DISTRIBUTION.toString())) {
                final CcDistributionStat distributionStat = new CcDistributionStat();
                gatherForPrevalence(distributionStat, rs);
                gatherForDistribution(distributionStat, rs);
                return distributionStat;
            } else if (StringUtils.equals(type, PREVALENCE.toString())){
                final CcPrevalenceStat prevalenceStat = new CcPrevalenceStat();
                gatherForPrevalence(prevalenceStat, rs);
                return prevalenceStat;
            }
            return null;
        });
    }

    private void gatherForPrevalence(final CcPrevalenceStat stat, final ResultSet rs) throws SQLException {
        Long generationId = rs.getLong("cc_generation_id");
        CcGenerationEntity ccGeneration = ccGenerationRepository.findOne(generationId);

        stat.setFaType(rs.getString("fa_type"));
        stat.setSourceKey(ccGeneration.getSource().getSourceKey());
        stat.setCohortId(rs.getInt("cohort_definition_id"));
        stat.setAnalysisId(rs.getInt("analysis_id"));
        stat.setAnalysisName(rs.getString("analysis_name"));
        stat.setResultType(PREVALENCE);
        stat.setCovariateId(rs.getLong("covariate_id"));
        stat.setCovariateName(rs.getString("covariate_name"));
        stat.setConceptName(rs.getString("concept_name"));
        stat.setTimeWindow(getTimeWindow(rs.getString("analysis_name")));
        stat.setConceptId(rs.getLong("concept_id"));
        stat.setAvg(rs.getDouble("avg_value"));
        stat.setCount(rs.getLong("count_value"));
        stat.setStrataId(rs.getLong("strata_id"));
        stat.setStrataName(rs.getString("strata_name"));
    }

    private void gatherForDistribution(final CcDistributionStat stat, final ResultSet rs) throws SQLException {
        stat.setResultType(DISTRIBUTION);
        stat.setAvg(rs.getDouble("avg_value"));
        stat.setStdDev(rs.getDouble("stdev_value"));
        stat.setMin(rs.getDouble("min_value"));
        stat.setP10(rs.getDouble("p10_value"));
        stat.setP25(rs.getDouble("p25_value"));
        stat.setMedian(rs.getDouble("median_value"));
        stat.setP75(rs.getDouble("p75_value"));
        stat.setP90(rs.getDouble("p90_value"));
        stat.setMax(rs.getDouble("max_value"));
        stat.setAggregateId(rs.getInt("aggregate_id"));
        stat.setAggregateName(rs.getString("aggregate_name"));
        stat.setMissingMeansZero(rs.getInt("missing_means_zero")==1);
    }

    public String getTimeWindow(String analysisName) {
        if (analysisName.endsWith("LongTerm")) return "Long Term";
        if (analysisName.endsWith("MediumTerm")) return "Medium Term";
        if (analysisName.endsWith("ShortTerm")) return "Short Term";
        if (analysisName.endsWith("AnyTimePrior")) return "Any Time Prior";
        if (analysisName.endsWith("Overlapping")) return "Overlapping";

        return "None";
    }

    private List<Integer> importAnalyses(final CohortCharacterizationEntity entity, final CohortCharacterizationEntity persistedEntity) {
        List<Integer> savedAnalysesIds = new ArrayList<>();
        final Map<String, FeAnalysisEntity> presetAnalysesMap = buildPresetAnalysisMap(entity);

        final Set<FeAnalysisEntity> analysesSet = new HashSet<>();

        for (final FeAnalysisEntity newAnalysis : entity.getFeatureAnalyses()) {
            switch (newAnalysis.getType()) {
                case CRITERIA_SET:
                    FeAnalysisWithCriteriaEntity<? extends FeAnalysisCriteriaEntity> criteriaAnalysis = (FeAnalysisWithCriteriaEntity) newAnalysis;
                    List<? extends FeAnalysisCriteriaEntity> design = criteriaAnalysis.getDesign();
                    Optional<FeAnalysisEntity> entityCriteriaSet = analysisService.findByCriteriaListAndCsAndDomainAndStat(design, criteriaAnalysis);
                    this.<FeAnalysisWithCriteriaEntity<?>>addAnalysis(savedAnalysesIds, analysesSet, criteriaAnalysis, entityCriteriaSet, a -> analysisService.createCriteriaAnalysis(a));
                    break;
                case PRESET:
                    analysesSet.add(presetAnalysesMap.get(newAnalysis.getDesign()));
                    break;
                case CUSTOM_FE:
                    FeAnalysisWithStringEntity withStringEntity = (FeAnalysisWithStringEntity) newAnalysis;
                    Optional<? extends FeAnalysisEntity> curAnalysis = analysisService.findByDesignAndName(withStringEntity, withStringEntity.getName());
                    this.<FeAnalysisEntity>addAnalysis(savedAnalysesIds, analysesSet, newAnalysis, curAnalysis, a -> analysisService.createAnalysis(a));
                    break;
                default:
                    throw new IllegalArgumentException("Analysis with type: " + newAnalysis.getType() + " cannot be imported");
            }
        }

        persistedEntity.setFeatureAnalyses(analysesSet);
        return savedAnalysesIds;
    }

    private <T extends FeAnalysisEntity<?>> void addAnalysis(List<Integer> savedAnalysesIds, Set<FeAnalysisEntity> entityAnalyses, T newAnalysis,
            Optional<? extends FeAnalysisEntity> curAnalysis, Function<T, FeAnalysisEntity> func) {
        if (curAnalysis.isPresent()) {
            entityAnalyses.add(curAnalysis.get());
        } else {
            newAnalysis.setName(NameUtils.getNameWithSuffix(newAnalysis.getName(), this::getFeNamesLike));
            FeAnalysisEntity created = func.apply(newAnalysis);
            entityAnalyses.add(created);
            savedAnalysesIds.add(created.getId());
        }
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
        final Set<CohortDefinition> cohortList = entity.getCohortDefinitions().stream()
                .map(designImportService::persistCohortOrGetExisting)
                .collect(Collectors.toSet());
        persistedEntity.setCohortDefinitions(cohortList);
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

    @PostConstruct
    public void init() {
        invalidateGenerations();
    }

    private void invalidateGenerations() {
        getTransactionTemplateRequiresNew().execute(transactionStatus -> {
            List<CcGenerationEntity> generations = findAllIncompleteGenerations();
            generations.forEach(gen -> {
                JobExecution job = jobService.getJobExecution(gen.getId());
                jobInvalidator.invalidationJobExecution(job);
            });
            return null;
        });
    }

    private List<String> getFeNamesLike(String name) {
        return analysisService.getNamesLike(name);
    }
}
