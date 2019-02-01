package org.ohdsi.webapi.cohortcharacterization;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.analysis.cohortcharacterization.design.CohortCharacterization;
import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisType;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.cohortcharacterization.converter.SerializedCcToCcConverter;
import org.ohdsi.webapi.cohortcharacterization.domain.CcGenerationEntity;
import org.ohdsi.webapi.cohortcharacterization.domain.CcParamEntity;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortcharacterization.dto.CcDistributionStat;
import org.ohdsi.webapi.cohortcharacterization.dto.CcPrevalenceStat;
import org.ohdsi.webapi.cohortcharacterization.dto.CcResult;
import org.ohdsi.webapi.cohortcharacterization.repository.AnalysisGenerationInfoEntityRepository;
import org.ohdsi.webapi.cohortcharacterization.repository.CcGenerationEntityRepository;
import org.ohdsi.webapi.cohortcharacterization.repository.CcParamRepository;
import org.ohdsi.webapi.cohortcharacterization.repository.CcRepository;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.common.DesignImportService;
import org.ohdsi.webapi.common.generation.GenerationUtils;
import org.ohdsi.webapi.feanalysis.FeAnalysisService;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisWithCriteriaEntity;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.service.CohortGenerationService;
import org.ohdsi.webapi.service.FeatureExtractionService;
import org.ohdsi.webapi.service.SourceService;
import org.ohdsi.webapi.shiro.annotations.CcGenerationId;
import org.ohdsi.webapi.shiro.annotations.DataSourceAccess;
import org.ohdsi.webapi.shiro.annotations.SourceKey;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.EntityUtils;
import org.ohdsi.webapi.util.SessionUtils;
import org.springframework.batch.core.*;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.ws.rs.NotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.ohdsi.webapi.Constants.GENERATE_COHORT_CHARACTERIZATION;
import static org.ohdsi.webapi.Constants.Params.*;

@Service
@Transactional
@DependsOn({"ccExportDTOToCcEntityConverter", "cohortDTOToCohortDefinitionConverter", "feAnalysisDTOToFeAnalysisConverter"})
public class CcServiceImpl extends AbstractDaoService implements CcService {

    private static final String GENERATION_NOT_FOUND_ERROR = "generation cannot be found by id %d";
    private static final String[] GENERATION_PARAMETERS = {"cdm_results_schema", "cohort_characterization_generation_id"};
    private final String QUERY_RESULTS = ResourceHelper.GetResourceAsString("/resources/cohortcharacterizations/sql/queryResults.sql");
    private final String DELETE_RESULTS = ResourceHelper.GetResourceAsString("/resources/cohortcharacterizations/sql/deleteResults.sql");
    private final String DELETE_EXECUTION = ResourceHelper.GetResourceAsString("/resources/cohortcharacterizations/sql/deleteExecution.sql");
    private final String IMPORTED_ENTITY_PREFIX = "COPY OF: ";

    private final static List<String> INCOMPLETE_STATUSES = ImmutableList.of(BatchStatus.STARTED, BatchStatus.STARTING, BatchStatus.STOPPING, BatchStatus.UNKNOWN)
            .stream().map(BatchStatus::name).collect(Collectors.toList());

    private CcRepository repository;
    private CcParamRepository paramRepository;
    private FeAnalysisService analysisService;
    private CohortDefinitionRepository cohortRepository;
    private JobTemplate jobTemplate;
    private CcGenerationEntityRepository ccGenerationRepository;
    private FeatureExtractionService featureExtractionService;
    private DesignImportService designImportService;
    private CohortGenerationService cohortGenerationService;
    private AnalysisGenerationInfoEntityRepository analysisGenerationInfoEntityRepository;
    private SourceService sourceService;
    private GenerationUtils generationUtils;

    private final JobRepository jobRepository;

    public CcServiceImpl(
            final CcRepository ccRepository,
            final CcParamRepository paramRepository,
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
            final GenerationUtils generationUtils
    ) {
        this.repository = ccRepository;
        this.paramRepository = paramRepository;
        this.analysisService = analysisService;
        this.cohortRepository = cohortRepository;
        this.jobTemplate = jobTemplate;
        this.ccGenerationRepository = ccGenerationRepository;
        this.featureExtractionService = featureExtractionService;
        this.designImportService = designImportService;
        this.cohortGenerationService = cohortGenerationService;
        this.jobRepository = jobRepository;
        this.analysisGenerationInfoEntityRepository = analysisGenerationInfoEntityRepository;
        this.sourceService = sourceService;
        this.generationUtils = generationUtils;
        SerializedCcToCcConverter.setConversionService(conversionService);
    }

    @Override
    public CohortCharacterizationEntity createCc(final CohortCharacterizationEntity entity) {
        entity.setCreatedBy(getCurrentUser());
        entity.setCreatedDate(new Date());
        return saveCc(entity);
    }

    private CohortCharacterizationEntity saveCc(final CohortCharacterizationEntity entity) {
        final CohortCharacterizationEntity savedEntity = repository.saveAndFlush(entity);

        gatherLinkedEntities(savedEntity);
        sortInnerEntities(savedEntity);

        final String serialized = this.serializeCc(savedEntity);
        savedEntity.setHashCode(serialized.hashCode());

        return repository.save(savedEntity);
    }

    public void deleteCc(Long ccId) {
        repository.delete(ccId);
    }

    private void sortInnerEntities(final CohortCharacterizationEntity savedEntity) {
        savedEntity.setFeatureAnalyses(new TreeSet<>(savedEntity.getFeatureAnalyses()));
    }

    @Override
    public CohortCharacterizationEntity updateCc(final CohortCharacterizationEntity entity) {
        final CohortCharacterizationEntity foundEntity = repository.findById(entity.getId())
                .orElseThrow(() -> new NotFoundException("CC entity isn't found"));

        updateLinkedFields(entity, foundEntity);

        if (StringUtils.isNotEmpty(entity.getName())) {
            foundEntity.setName(entity.getName());
        }

        foundEntity.setFeatureAnalyses(entity.getFeatureAnalyses());
        foundEntity.setCohortDefinitions(entity.getCohortDefinitions());
        foundEntity.setParameters(entity.getParameters());

        foundEntity.setModifiedDate(new Date());
        foundEntity.setModifiedBy(getCurrentUser());

        return saveCc(foundEntity);
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
        newCohortCharacterization.setName(IMPORTED_ENTITY_PREFIX + entity.getName());
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
        return new SerializedCcToCcConverter().convertToDatabaseColumn(cohortCharacterizationEntity);
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

    @Override
    @DataSourceAccess
    public CohortCharacterization findDesignByGenerationId(@CcGenerationId final Long id) {
        return ccGenerationRepository.findById(id).map(gen -> gen.getDesign()).orElse(null);
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
        builder.addString(TARGET_TABLE, GenerationUtils.getTempCohortTableName());

        final JobParameters jobParameters = builder.toJobParameters();

        JdbcTemplate jdbcTemplate = getSourceJdbcTemplate(source);

        Job generateCohortJob = generationUtils.buildJobForCohortBasedAnalysisTasklet(
                GENERATE_COHORT_CHARACTERIZATION,
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

        return this.jobTemplate.launch(generateCohortJob, jobParameters);
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
    public List<CcResult> findResults(@CcGenerationId final Long generationId) {
        final CcGenerationEntity generationEntity = ccGenerationRepository.findById(generationId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(GENERATION_NOT_FOUND_ERROR, generationId)));
        final Source source = generationEntity.getSource();
        final String resultSchema = source.getTableQualifier(SourceDaimon.DaimonType.Results);
        String generationResults = SqlRender.renderSql(
                QUERY_RESULTS,
                GENERATION_PARAMETERS,
                new String[]{resultSchema, String.valueOf(generationId)}
        );
        String translatedSql = SqlTranslate.translateSql(generationResults, source.getSourceDialect(), SessionUtils.sessionId(), resultSchema);
        return getGenerationResults(source, translatedSql);
    }

    @Override
    @DataSourceAccess
    public void deleteCcGeneration(@CcGenerationId Long generationId) {
        final CcGenerationEntity generationEntity = ccGenerationRepository.findById(generationId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(GENERATION_NOT_FOUND_ERROR, generationId)));
        final Source source = generationEntity.getSource();
        final String resultSchema = source.getTableQualifier(SourceDaimon.DaimonType.Results);
        final String sql = SqlRender.renderSql(
                DELETE_RESULTS,
                GENERATION_PARAMETERS,
                new String[]{ resultSchema, String.valueOf(generationId) }
        );
        final String translatedSql = SqlTranslate.translateSql(sql, source.getSourceDialect(), SessionUtils.sessionId(), resultSchema);
        getSourceJdbcTemplate(source).execute(translatedSql);

        final String deleteJobSql = SqlRender.renderSql(
                DELETE_EXECUTION,
                new String[]{ "ohdsiSchema", "execution_id" },
                new String[]{ getOhdsiSchema(), String.valueOf(generationId) }
        );
        final String translatedJobSql = SqlTranslate.translateSql(deleteJobSql, getDialect());
        getJdbcTemplate().batchUpdate(translatedJobSql.split(";"));
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
        stat.setTimeWindow(featureExtractionService.getTimeWindow(rs.getString("analysis_name")));
        stat.setConceptId(rs.getLong("concept_id"));
        stat.setAvg(rs.getDouble("avg_value"));
        stat.setCount(rs.getLong("count_value"));
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
        final List<CohortDefinition> cohortList = entity.getCohortDefinitions().stream()
                .map(designImportService::persistCohortOrGetExisting)
                .collect(Collectors.toList());
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
                JobExecution job = cohortGenerationService.getJobExecution(gen.getId());
                job.setStatus(BatchStatus.FAILED);
                job.setExitStatus(new ExitStatus(ExitStatus.FAILED.getExitCode(), "Invalidated by system"));
                jobRepository.update(job);
            });
            return null;
        });
    }
}
