/*
 * Copyright 2017 Observational Health Data Sciences and Informatics <OHDSI.org>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ohdsi.webapi.cohortcharacterization;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisType;
import org.ohdsi.circe.cohortdefinition.*;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.featureExtraction.FeatureExtraction;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.cohortcharacterization.domain.AnalysisGenerationInfoEntity;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortcharacterization.repository.AnalysisGenerationInfoEntityRepository;
import org.ohdsi.webapi.feanalysis.FeAnalysisService;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisWithCriteriaEntity;
import org.ohdsi.webapi.service.ConceptSetService;
import org.ohdsi.webapi.service.SourceService;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.SessionUtils;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

import static org.ohdsi.webapi.Constants.Params.*;

public class GenerateCohortCharacterizationTasklet implements StoppableTasklet {
    private static final Log log = LogFactory.getLog(GenerateCohortCharacterizationTasklet.class);
    private static final String[] CUSTOM_PARAMETERS = {"analysisId", "analysisName", "cohortId", "jobId", "design"};
    private static final String[] RETRIEVING_PARAMETERS = {"features", "featureRefs", "analysisRefs", "cohortId", "executionId"};

    private volatile boolean stopped = false;
    private final long checkInterval = 1000L;
    
    private final TransactionTemplate transactionTemplate;
    private final ExecutorService taskExecutor;
    private final JdbcTemplate jdbcTemplate;
    private final CcService ccService;
    private final FeAnalysisService feAnalysisService;
    private final AnalysisGenerationInfoEntityRepository analysisGenerationInfoEntityRepository;
    private final SourceService sourceService;
    private final UserRepository userRepository;
    private final ConceptSetService conceptSetService;
    
    public GenerateCohortCharacterizationTasklet(
            final JdbcTemplate jdbcTemplate,
            final TransactionTemplate transactionTemplate,
            final CcService ccService,
            final FeAnalysisService feAnalysisService,
            final AnalysisGenerationInfoEntityRepository analysisGenerationInfoEntityRepository,
            final SourceService sourceService,
            final UserRepository userRepository,
            ConceptSetService conceptSetService) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
        this.ccService = ccService;
        this.feAnalysisService = feAnalysisService;
        this.analysisGenerationInfoEntityRepository = analysisGenerationInfoEntityRepository;
        this.sourceService = sourceService;
        this.userRepository = userRepository;
        this.conceptSetService = conceptSetService;
        this.taskExecutor = Executors.newSingleThreadExecutor();
    }

    private int[] doTask(ChunkContext chunkContext) {
        initTx();
        new CcTask(chunkContext).run();
        return null;
    }

    private void initTx() {
        DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition();
        txDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus initStatus = this.transactionTemplate.getTransactionManager().getTransaction(txDefinition);
        this.transactionTemplate.getTransactionManager().commit(initStatus);
    }

    interface CriteriaVisitor<T extends Criteria> {
        Integer getConceptSetId(T criteria);
    }

    class DrugEraVisitor implements CriteriaVisitor<DrugEra> {

        @Override
        public Integer getConceptSetId(DrugEra drugEra) {
            return drugEra.codesetId;
        }
    }

    class ConditionEraVisitor implements CriteriaVisitor<ConditionEra> {

        @Override
        public Integer getConceptSetId(ConditionEra conditionEra) {
            return conditionEra.codesetId;
        }
    }

    class ConditionOccurrenceVisitor implements CriteriaVisitor<ConditionOccurrence> {

        @Override
        public Integer getConceptSetId(ConditionOccurrence conditionOccurrence) {
            return conditionOccurrence.codesetId;
        }
    }

    class DeathVisitor implements CriteriaVisitor<Death> {

        @Override
        public Integer getConceptSetId(Death death) {
            return death.codesetId;
        }
    }

    class DeviceExposureVisitor implements CriteriaVisitor<DeviceExposure> {

        @Override
        public Integer getConceptSetId(DeviceExposure deviceExposure) {
            return deviceExposure.codesetId;
        }
    }

    class DoseEraVisitor implements CriteriaVisitor<DoseEra> {

        @Override
        public Integer getConceptSetId(DoseEra doseEra) {
            return doseEra.codesetId;
        }
    }

    class DrugExposureVisitor implements CriteriaVisitor<DrugExposure> {

        @Override
        public Integer getConceptSetId(DrugExposure drugExposure) {
            return drugExposure.codesetId;
        }
    }

    class MeasurementVisitor implements CriteriaVisitor<Measurement> {

        @Override
        public Integer getConceptSetId(Measurement measurement) {
            return measurement.codesetId;
        }
    }

    class ObservationVisitor implements CriteriaVisitor<Observation> {

        @Override
        public Integer getConceptSetId(Observation observation) {
            return observation.codesetId;
        }
    }

    class ProcedureOccurrenceVisitor implements CriteriaVisitor<ProcedureOccurrence> {

        @Override
        public Integer getConceptSetId(ProcedureOccurrence procedureOccurrence) {
            return procedureOccurrence.codesetId;
        }
    }

    class SpecimenVisitor implements CriteriaVisitor<Specimen> {

        @Override
        public Integer getConceptSetId(Specimen specimen) {
            return specimen.codesetId;
        }
    }

    class VisitOccurrenceVisitor implements CriteriaVisitor<VisitOccurrence> {

        @Override
        public Integer getConceptSetId(VisitOccurrence visitOccurrence) {
            return visitOccurrence.codesetId;
        }
    }

    private Integer visitCriteria(Criteria criteria) {
        Integer result = 0;
        if (criteria instanceof DrugEra) {
            result = new DrugEraVisitor().getConceptSetId((DrugEra) criteria);
        } else if (criteria instanceof ConditionEra) {
            result = new ConditionEraVisitor().getConceptSetId((ConditionEra) criteria);
        } else if (criteria instanceof ConditionOccurrence) {
            result = new ConditionOccurrenceVisitor().getConceptSetId((ConditionOccurrence) criteria);
        } else if (criteria instanceof Death) {
            result = new DeathVisitor().getConceptSetId((Death) criteria);
        } else if (criteria instanceof DeviceExposure) {
            result = new DeviceExposureVisitor().getConceptSetId((DeviceExposure) criteria);
        } else if (criteria instanceof DoseEra) {
            result = new DoseEraVisitor().getConceptSetId((DoseEra) criteria);
        } else if (criteria instanceof DrugExposure) {
            result = new DrugExposureVisitor().getConceptSetId((DrugExposure) criteria);
        } else if (criteria instanceof Measurement) {
            result = new MeasurementVisitor().getConceptSetId((Measurement) criteria);
        } else if (criteria instanceof Observation) {
            result = new ObservationVisitor().getConceptSetId((Observation) criteria);
        } else if (criteria instanceof ProcedureOccurrence) {
            result = new ProcedureOccurrenceVisitor().getConceptSetId((ProcedureOccurrence) criteria);
        } else if (criteria instanceof Specimen) {
            result = new SpecimenVisitor().getConceptSetId((Specimen) criteria);
        } else if (criteria instanceof VisitOccurrence) {
            result = new VisitOccurrenceVisitor().getConceptSetId((VisitOccurrence) criteria);
        }
        return result;
    }
    
    private class CcTask {

        final String prevalenceRetrievingQuery = ResourceHelper.GetResourceAsString("/resources/cohortcharacterizations/sql/prevalenceRetrieving.sql");
        
        final String distributionRetrievingQuery = ResourceHelper.GetResourceAsString("/resources/cohortcharacterizations/sql/distributionRetrieving.sql");
        
        final String customDistributionQueryWrapper = ResourceHelper.GetResourceAsString("/resources/cohortcharacterizations/sql/customDistribution.sql");
        
        final String customPrevalenceQueryWrapper = ResourceHelper.GetResourceAsString("/resources/cohortcharacterizations/sql/customPrevalence.sql");

        final CohortCharacterizationEntity cohortCharacterization;
        final Source source;
        final UserEntity userEntity;
        final String cohortTable;
        
        private final Long jobId;

        CcTask(final ChunkContext context) {
            Map<String, Object> jobParams = context.getStepContext().getJobParameters();
            this.cohortCharacterization = ccService.findByIdWithLinkedEntities(
                    Long.valueOf(jobParams.get(COHORT_CHARACTERIZATION_ID).toString())
            );
            this.jobId = context.getStepContext().getStepExecution().getJobExecution().getId();
            this.source = sourceService.findBySourceId(
                    Integer.valueOf(jobParams.get(SOURCE_ID).toString())
            );
            this.cohortTable = jobParams.get(TARGET_TABLE).toString();
            this.userEntity = userRepository.findByLogin(jobParams.get(JOB_AUTHOR).toString());
        }
        
        private void run() {

            saveInfo(jobId, cohortCharacterization, userEntity);
            cohortCharacterization.getCohortDefinitions()
                    .forEach(definition -> runAnalysisOnCohort(definition.getId()));
        }

        private void saveInfo(Long jobId, CohortCharacterizationEntity cohortCharacterization, UserEntity userEntity) {

            AnalysisGenerationInfoEntity generationInfoEntity = new AnalysisGenerationInfoEntity();
            generationInfoEntity.setId(jobId);
            generationInfoEntity.setDesign(cohortCharacterization);
            generationInfoEntity.setHashCode(cohortCharacterization.getHashCode());
            generationInfoEntity.setCreatedBy(userEntity);
            analysisGenerationInfoEntityRepository.save(generationInfoEntity);
        }

        private int[] runAnalysisOnCohort(final Integer cohortDefinitionId) {
            FutureTask<int[]> batchUpdateTask = new FutureTask<>(
                    () -> jdbcTemplate.batchUpdate(
                            getSqlQueriesToRun(
                                    createFeJsonObject(
                                            createDefaultOptions(cohortDefinitionId)
                                    ), 
                                    cohortDefinitionId
                            )
                    )
            );
            taskExecutor.execute(batchUpdateTask);
            try {
                while (true) {
                    Thread.sleep(checkInterval);
                    if (batchUpdateTask.isDone()) {
                        return batchUpdateTask.get();
                    } else if (stopped) {
                        batchUpdateTask.cancel(true);
                        return null;
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private ConceptSet getConceptSet(Integer id) {
            ConceptSet conceptSet = new ConceptSet();
            conceptSet.id = id;
            conceptSet.name = conceptSetService.getConceptSet(id).getName();
            conceptSet.expression = conceptSetService.getConceptSetExpression(id);
            return conceptSet;
        }
        
        private List<String> getQueriesForCustomDistributionAnalyses(final Integer cohortId) {
            return cohortCharacterization.getFeatureAnalyses()
                    .stream()
                    .filter(FeAnalysisEntity::isCustom)
                    .filter(v -> v.getStatType() == CcResultType.DISTRIBUTION)
                    .map(v -> SqlRender.renderSql(customDistributionQueryWrapper,
                            CUSTOM_PARAMETERS,
                            new String[] { String.valueOf(v.getId()), org.springframework.util.StringUtils.quote(v.getName()), String.valueOf(cohortId), String.valueOf(jobId), v.getDesign()} ))
                    .collect(Collectors.toList());
        }
        
        private List<String> getQueriesForCustomPrevalenceAnalyses(final Integer cohortId) {
            return cohortCharacterization.getFeatureAnalyses()
                    .stream()
                    .filter(FeAnalysisEntity::isCustom)
                    .filter(v -> v.getStatType() == CcResultType.PREVALENCE)
                    .map(v -> SqlRender.renderSql(customPrevalenceQueryWrapper,
                            CUSTOM_PARAMETERS,
                            new String[] { String.valueOf(v.getId()), org.springframework.util.StringUtils.quote(v.getName()), String.valueOf(cohortId), String.valueOf(jobId), v.getDesign()} ))
                    .collect(Collectors.toList());
        }

        private List<FeAnalysisWithCriteriaEntity> getFeAnalysesWithCriteria() {

            return cohortCharacterization.getFeatureAnalyses().stream()
                    .filter(fa -> StandardFeatureAnalysisType.CRITERIA_SET.equals(fa.getType()))
                    .map(fa -> (FeAnalysisWithCriteriaEntity)fa)
                    .collect(Collectors.toList());
        }

        private List<String> getQueriesForCodeset(List<FeAnalysisWithCriteriaEntity> analysesWithCriteria) {

            CohortExpressionQueryBuilder queryBuilder = new CohortExpressionQueryBuilder();
            String codesetInsertsQuery = queryBuilder.getCodesetQuery(analysesWithCriteria.stream()
                    .map(FeAnalysisWithCriteriaEntity::getDesign)
                    .flatMap(Collection::stream)
                    .flatMap(criterion -> Arrays.stream(criterion.getExpression().criteriaList))
                    .map(criteria -> visitCriteria(criteria.criteria))
                    .filter(id -> id > 0)
                    .map(this::getConceptSet)
                    .toArray(ConceptSet[]::new));
            return Collections.singletonList(codesetInsertsQuery);
        }

        private List<String> getQueriesForCriteriaAnalyses(final Integer cohortId, List<FeAnalysisWithCriteriaEntity> analysesWithCriteria) {

            CohortExpressionQueryBuilder queryBuilder = new CohortExpressionQueryBuilder();
            return analysesWithCriteria.stream()
                    .map(FeAnalysisWithCriteriaEntity::getDesign)
                    .flatMap(Collection::stream)
                    .map(fe -> queryBuilder.getCriteriaGroupQuery(fe.getExpression(), "#events"))
                    .collect(Collectors.toList());
        }

        private List<String> getQueriesForResultsRetrieving(final JSONObject jsonObject, final Integer cohortId) {
            final String cohortWrapper = "select %1$d as %2$s from (%3$s) W";

            final String featureRefColumns = "cohort_definition_id, covariate_id, covariate_name, analysis_id, concept_id";
            final String featureRefs = String.format(cohortWrapper, cohortId, featureRefColumns,
                    StringUtils.stripEnd(jsonObject.getString("sqlQueryFeatureRef"), ";"));

            final String analysisRefColumns = "cohort_definition_id, CAST(analysis_id AS INT) analysis_id, analysis_name, domain_id, start_day, end_day, CAST(is_binary AS CHAR(1)) is_binary,CAST(missing_means_zero AS CHAR(1)) missing_means_zero";
            final String analysisRefs = String.format(cohortWrapper, cohortId, analysisRefColumns,
                    StringUtils.stripEnd(jsonObject.getString("sqlQueryAnalysisRef"), ";"));

            final List<String> queries = new ArrayList<>();

            if (ccHasPresetDistributionAnalyses()) {
                final String distColumns = "cohort_definition_id, covariate_id, count_value, min_value, max_value, average_value, "
                        + "standard_deviation, median_value, p10_value, p25_value, p75_value, p90_value";
                final String distFeatures = String.format(cohortWrapper, cohortId, distColumns,
                        StringUtils.stripEnd(jsonObject.getString("sqlQueryContinuousFeatures"), ";"));
                final String query = SqlRender.renderSql(distributionRetrievingQuery, RETRIEVING_PARAMETERS,
                        new String[] { distFeatures, featureRefs, analysisRefs, String.valueOf(cohortId), String.valueOf(jobId) });
                queries.add(query);
            }
            if (ccHasPresetPrevalenceAnalyses()) {
                final String featureColumns = "cohort_definition_id, covariate_id, sum_value, average_value";
                final String features = String.format(cohortWrapper, cohortId, featureColumns,
                        StringUtils.stripEnd(jsonObject.getString("sqlQueryFeatures"), ";"));
                final String query = SqlRender.renderSql(prevalenceRetrievingQuery, RETRIEVING_PARAMETERS,
                        new String[]{ features, featureRefs, analysisRefs, String.valueOf(cohortId), String.valueOf(jobId) });
                queries.add(query);
            }

            return queries;
        }

        private boolean ccHasPresetPrevalenceAnalyses() {
            return cohortCharacterization.getFeatureAnalyses()
                    .stream()
                    .anyMatch(analysis -> analysis.isPreset() && analysis.getStatType() == CcResultType.PREVALENCE);
        }

        private boolean ccHasPresetDistributionAnalyses() {
            return cohortCharacterization.getFeatureAnalyses()
                    .stream()
                    .anyMatch(analysis -> analysis.isPreset() && analysis.getStatType() == CcResultType.DISTRIBUTION);
        }

        private CohortExpressionQueryBuilder.BuildExpressionQueryOptions createDefaultOptions(final Integer id) {
            final CohortExpressionQueryBuilder.BuildExpressionQueryOptions options = new CohortExpressionQueryBuilder.BuildExpressionQueryOptions();
            options.cdmSchema = source.getTableQualifier(SourceDaimon.DaimonType.CDM);
            options.resultSchema = source.getTableQualifier(SourceDaimon.DaimonType.Results);
            options.cohortId = id;
            return options;
        }

        private String[] getSqlQueriesToRun(final JSONObject jsonObject, final Integer cohortDefinitionId) {
            final StringJoiner joiner = new StringJoiner("\n\n");

            joiner.add(jsonObject.getString("sqlConstruction"));

            getQueriesForResultsRetrieving(jsonObject,cohortDefinitionId).forEach(joiner::add);
            getQueriesForCustomDistributionAnalyses(cohortDefinitionId).forEach(joiner::add);
            getQueriesForCustomPrevalenceAnalyses(cohortDefinitionId).forEach(joiner::add);

            List<FeAnalysisWithCriteriaEntity> analysesWithCriteria = getFeAnalysesWithCriteria();
            if (!analysesWithCriteria.isEmpty()) {
                getQueriesForCodeset(analysesWithCriteria).forEach(joiner::add);
                getQueriesForCriteriaAnalyses(cohortDefinitionId, analysesWithCriteria).forEach(joiner::add);
            }

            joiner.add(jsonObject.getString("sqlCleanup"));

            final String sql = SqlRender.renderSql(joiner.toString(),
                    new String[]{RESULTS_DATABASE_SCHEMA, CDM_DATABASE_SCHEMA, VOCABULARY_DATABASE_SCHEMA},
                    new String[]{source.getTableQualifier(SourceDaimon.DaimonType.Results), source.getTableQualifier(SourceDaimon.DaimonType.CDM),
                        source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary)});
            final String translatedSql = SqlTranslate.translateSql(sql, source.getSourceDialect(), SessionUtils.sessionId(), null);
            return SqlSplit.splitSql(translatedSql);
        }

        private JSONObject createFeJsonObject(final CohortExpressionQueryBuilder.BuildExpressionQueryOptions options) {
            FeatureExtraction.init(null);
            String settings = buildSettings();
            String sqlJson = FeatureExtraction.createSql(settings, true, options.resultSchema + "." + cohortTable,
                    "subject_id", options.cohortId, options.cdmSchema);
            return new JSONObject(sqlJson);
        }
        
        private String buildSettings() {

            final JSONObject defaultSettings = new JSONObject(FeatureExtraction.getDefaultPrespecAnalyses());

            feAnalysisService.findAllPresetAnalyses().forEach(v -> defaultSettings.remove(v.getDesign()));
            
            cohortCharacterization.getParameters().forEach(param -> defaultSettings.put(param.getName(), param.getValue()));
            cohortCharacterization.getFeatureAnalyses()
                    .stream()
                    .filter(FeAnalysisEntity::isPreset)
                    .forEach(analysis -> defaultSettings.put(analysis.getDesign(), Boolean.TRUE));
            
            return defaultSettings.toString();
        }
        
    }
    
    @Override
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
        try {
            this.transactionTemplate.execute(status -> doTask(chunkContext));
        } catch (final TransactionException e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            taskExecutor.shutdown();
        }
        return RepeatStatus.FINISHED;
    }

    @Override
    public void stop() {
        this.stopped = true;
    }
}
