package org.ohdsi.webapi.cohortcharacterization;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.analysis.cohortcharacterization.design.CohortCharacterization;
import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisType;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.cohortcharacterization.converter.SerializedCcToCcConverter;
import org.ohdsi.webapi.cohortcharacterization.domain.*;
import org.ohdsi.webapi.cohortcharacterization.dto.CcDistributionStat;
import org.ohdsi.webapi.cohortcharacterization.dto.CcPrevalenceStat;
import org.ohdsi.webapi.cohortcharacterization.dto.CcResult;
import org.ohdsi.webapi.cohortcharacterization.repository.*;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.common.DesignImportService;
import org.ohdsi.webapi.common.generation.GenerationUtils;
import org.ohdsi.webapi.feanalysis.FeAnalysisService;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisWithCriteriaEntity;
import org.ohdsi.webapi.job.GeneratesNotification;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.model.WithId;
import org.ohdsi.webapi.service.*;
import org.ohdsi.webapi.shiro.annotations.CcGenerationId;
import org.ohdsi.webapi.shiro.annotations.DataSourceAccess;
import org.ohdsi.webapi.shiro.annotations.SourceKey;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.sqlrender.SourceAwareSqlRender;
import org.ohdsi.webapi.util.CancelableJdbcTemplate;
import org.ohdsi.webapi.util.CopyUtils;
import org.ohdsi.webapi.util.EntityUtils;
import org.ohdsi.webapi.util.SessionUtils;
import org.ohdsi.webapi.util.SourceUtils;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.ws.rs.NotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.ohdsi.webapi.Constants.GENERATE_COHORT_CHARACTERIZATION;
import static org.ohdsi.webapi.Constants.Params.*;

@Service
@Transactional
@DependsOn({"ccExportDTOToCcEntityConverter", "cohortDTOToCohortDefinitionConverter", "feAnalysisDTOToFeAnalysisConverter"})
public class CcServiceImpl extends AbstractDaoService implements CcService, GeneratesNotification {

    private static final String GENERATION_NOT_FOUND_ERROR = "generation cannot be found by id %d";
    private static final String[] PARAMETERS_RESULTS = {"cohort_characterization_generation_id", "threshold_level", "vocabulary_schema"};
    private static final String[] PREVALENCE_STATS_PARAMS = {"cdm_database_schema", "cdm_results_schema", "cc_generation_id", "analysis_id", "cohort_id", "covariate_id"};
    private final String QUERY_RESULTS = ResourceHelper.GetResourceAsString("/resources/cohortcharacterizations/sql/queryResults.sql");
    private final String DELETE_RESULTS = ResourceHelper.GetResourceAsString("/resources/cohortcharacterizations/sql/deleteResults.sql");
    private final String DELETE_EXECUTION = ResourceHelper.GetResourceAsString("/resources/cohortcharacterizations/sql/deleteExecution.sql");
    private final String QUERY_PREVALENCE_STATS = ResourceHelper.GetResourceAsString("/resources/cohortcharacterizations/sql/queryCovariateStatsVocab.sql");

    private final static List<String> INCOMPLETE_STATUSES = ImmutableList.of(BatchStatus.STARTED, BatchStatus.STARTING, BatchStatus.STOPPING, BatchStatus.UNKNOWN)
            .stream().map(BatchStatus::name).collect(Collectors.toList());

    private final EntityGraph defaultEntityGraph = EntityUtils.fromAttributePaths(
            "cohortDefinitions",
            "featureAnalyses",
            "stratas",
            "parameters",
            "createdBy",
            "modifiedBy"
    );

    private CcRepository repository;
    private CcParamRepository paramRepository;
    private CcStrataRepository strataRepository;
    private CcConceptSetRepository conceptSetRepository;
    private FeAnalysisService analysisService;
    private CohortDefinitionRepository cohortRepository;
    private CcGenerationEntityRepository ccGenerationRepository;
    private FeatureExtractionService featureExtractionService;
    private DesignImportService designImportService;
    private CohortGenerationService cohortGenerationService;
    private AnalysisGenerationInfoEntityRepository analysisGenerationInfoEntityRepository;
    private SourceService sourceService;
    private GenerationUtils generationUtils;
    private EntityManager entityManager;
    private ApplicationEventPublisher eventPublisher;

    private final JobRepository jobRepository;
    private final SourceAwareSqlRender sourceAwareSqlRender;
    private final JobService jobService;

    public CcServiceImpl(
            final CcRepository ccRepository,
            final CcParamRepository paramRepository,
            final CcStrataRepository strataRepository,
            final CcConceptSetRepository conceptSetRepository,
            final FeAnalysisService analysisService,
            final CohortDefinitionRepository cohortRepository,
            final JobTemplate jobTemplate,
            final CcGenerationEntityRepository ccGenerationRepository,
            final FeatureExtractionService featureExtractionService,
            final ConversionService conversionService,
            final DesignImportService designImportService,
            final CohortGenerationService cohortGenerationService,
            final JobRepository jobRepository,
            final AnalysisGenerationInfoEntityRepository analysisGenerationInfoEntityRepository,
            final SourceService sourceService,
            final GenerationUtils generationUtils,
            SourceAwareSqlRender sourceAwareSqlRender,
            final EntityManager entityManager,
            final JobService jobService,
            final ApplicationEventPublisher eventPublisher
    ) {
        this.repository = ccRepository;
        this.paramRepository = paramRepository;
        this.strataRepository = strataRepository;
        this.conceptSetRepository = conceptSetRepository;
        this.analysisService = analysisService;
        this.cohortRepository = cohortRepository;
        this.ccGenerationRepository = ccGenerationRepository;
        this.featureExtractionService = featureExtractionService;
        this.designImportService = designImportService;
        this.cohortGenerationService = cohortGenerationService;
        this.jobRepository = jobRepository;
        this.analysisGenerationInfoEntityRepository = analysisGenerationInfoEntityRepository;
        this.sourceService = sourceService;
        this.generationUtils = generationUtils;
        this.sourceAwareSqlRender = sourceAwareSqlRender;
        this.entityManager = entityManager;
        this.jobService = jobService;
        this.eventPublisher = eventPublisher;
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
        CcStrataConceptSetEntity savedEntity = conceptSetRepository.save(entity.getConceptSetEntity());
        foundEntity.setConceptSetEntity(savedEntity);
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

        importCohorts(entity, persistedCohortCharacterization);
        importAnalyses(entity, persistedCohortCharacterization);

        final CohortCharacterizationEntity savedEntity = saveCc(persistedCohortCharacterization);

        eventPublisher.publishEvent(new CcImportEvent(savedEntity));

        return savedEntity;
    }

    @Override
    public String getNameForCopy(String dtoName) {
        return CopyUtils.getNameForCopy(dtoName, this::countLikeName, repository.findByName(dtoName));
    }

    @Override
    public String serializeCc(final Long id) {
        final CohortCharacterizationEntity cohortCharacterizationEntity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cohort characterization cannot be found by id: " + id));
        return this.serializeCc(cohortCharacterizationEntity);
    }

    @Override
    public String serializeCc(final CohortCharacterizationEntity cohortCharacterizationEntity) {
        return new SerializedCcToCcConverter().convertToDatabaseColumn(cohortCharacterizationEntity);
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
        return ccGenerationRepository.findById(id).map(gen -> gen.getDesign()).orElse(null);
    }

    @Override
    public Page<CohortCharacterizationEntity> getPageWithLinkedEntities(final Pageable pageable) {
        return repository.findAll(pageable, defaultEntityGraph);
    }

    @Override
    public Page<CohortCharacterizationEntity> getPage(final Pageable pageable) {
        return repository.findAll(pageable);
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
                        analysisService,
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

    @Override
    @DataSourceAccess
    public List<CcResult> findResults(@CcGenerationId final Long generationId, float thresholdLevel) {
        final CcGenerationEntity generationEntity = ccGenerationRepository.findById(generationId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(GENERATION_NOT_FOUND_ERROR, generationId)));
        final Source source = generationEntity.getSource();
        String generationResults = sourceAwareSqlRender.renderSql(source.getSourceId(), QUERY_RESULTS, PARAMETERS_RESULTS, 
                new String[]{String.valueOf(generationId), String.valueOf(thresholdLevel), SourceUtils.getVocabularyQualifier(source)});
        final String tempSchema = SourceUtils.getTempQualifier(source);
        String translatedSql = SqlTranslate.translateSql(generationResults, source.getSourceDialect(), SessionUtils.sessionId(), tempSchema);
        return getGenerationResults(source, translatedSql);
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
      jobService.cancelJobExecution(getJobName(), j -> {
        JobParameters jobParameters = j.getJobParameters();
        return Objects.equals(jobParameters.getString(SOURCE_ID), Integer.toString(source.getSourceId()))
                && Objects.equals(jobParameters.getString(COHORT_CHARACTERIZATION_ID), Long.toString(id));
      });
    }

    @Override
    public int countLikeName(String copyName) {

      return repository.countByNameStartsWith(copyName);
    }

    @Override
    public String getJobName() {
        return GENERATE_COHORT_CHARACTERIZATION;
    }

    @Override
    public String getExecutionFoldingKey() {
        return COHORT_CHARACTERIZATION_ID;
    }

    private List<CcResult> getGenerationResults(final Source source, final String translatedSql) {
        return this.getSourceJdbcTemplate(source).query(translatedSql, (rs, rowNum) -> {
            final String type = rs.getString("type");
            if (StringUtils.equals(type, CcResultType.DISTRIBUTION.toString())) {
                final CcDistributionStat distributionStat = new CcDistributionStat();
                gatherForPrevalence(distributionStat, rs);
                gatherForDistribution(distributionStat, rs);
                return distributionStat;
            } else if (StringUtils.equals(type, CcResultType.PREVALENCE.toString())){
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
        stat.setResultType(CcResultType.PREVALENCE);
        stat.setCovariateId(rs.getLong("covariate_id"));
        stat.setCovariateName(rs.getString("covariate_name"));
        stat.setConceptName(rs.getString("concept_name"));
        stat.setTimeWindow(featureExtractionService.getTimeWindow(rs.getString("analysis_name")));
        stat.setConceptId(rs.getLong("concept_id"));
        stat.setAvg(rs.getDouble("avg_value"));
        stat.setCount(rs.getLong("count_value"));
        stat.setStrataId(rs.getLong("strata_id"));
        stat.setStrataName(rs.getString("strata_name"));
    }

    private void gatherForDistribution(final CcDistributionStat stat, final ResultSet rs) throws SQLException {
        stat.setResultType(CcResultType.DISTRIBUTION);
        stat.setAvg(rs.getDouble("avg_value"));
        stat.setStdDev(rs.getDouble("stdev_value"));
        stat.setMin(rs.getDouble("min_value"));
        stat.setP10(rs.getDouble("p10_value"));
        stat.setP25(rs.getDouble("p25_value"));
        stat.setMedian(rs.getDouble("median_value"));
        stat.setP75(rs.getDouble("p75_value"));
        stat.setP90(rs.getDouble("p90_value"));
        stat.setMax(rs.getDouble("max_value"));
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
                job.setStatus(BatchStatus.FAILED);
                job.setExitStatus(new ExitStatus(ExitStatus.FAILED.getExitCode(), "Invalidated by system"));
                jobRepository.update(job);
            });
            return null;
        });
    }
}
