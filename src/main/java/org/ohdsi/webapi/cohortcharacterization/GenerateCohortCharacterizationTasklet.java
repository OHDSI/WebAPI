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

import static org.ohdsi.webapi.Constants.Params.CDM_DATABASE_SCHEMA;
import static org.ohdsi.webapi.Constants.Params.COHORT_CHARACTERIZATION_ID;
import static org.ohdsi.webapi.Constants.Params.RESULTS_DATABASE_SCHEMA;
import static org.ohdsi.webapi.Constants.Params.TARGET_DIALECT;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder;
import org.ohdsi.featureExtraction.FeatureExtraction;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.feanalysis.FeAnalysisService;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
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

public class GenerateCohortCharacterizationTasklet implements StoppableTasklet {
    private static final Log log = LogFactory.getLog(GenerateCohortCharacterizationTasklet.class);

    private volatile boolean stopped = false;
    private final long checkInterval = 1000L;
    
    private final TransactionTemplate transactionTemplate;
    private final ExecutorService taskExecutor;
    private final JdbcTemplate jdbcTemplate;
    private final CcService ccService;
    private final FeAnalysisService feAnalysisService;
    
    
    public GenerateCohortCharacterizationTasklet(
            final JdbcTemplate jdbcTemplate,
            final TransactionTemplate transactionTemplate,
            final CcService ccService,
            final FeAnalysisService feAnalysisService
            ) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
        this.ccService = ccService;
        this.feAnalysisService = feAnalysisService;
        
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
    
    private class CcTask {

        final String prevalenceRetrievingQuery = "\n" +
                " with " +
                "     features as (%1$s), " +
                "     feature_refs as (%2$s), " +
                "     analysis_refs as(%3$s) " +
                " insert into @results_database_schema.cc_results " +
                " (type, covariate_id, covariate_name, analysis_id, analysis_name, concept_id, count_value, avg_value, cohort_definition_id, cohort_characterization_generation_id) " +
                " select 'PREVALENCE' as type, " +
                "       f.covariate_id, " +
                "       fr.covariate_name, " +
                "       ar.analysis_id, " +
                "       ar.analysis_name, " +
                "       fr.concept_id, " +
                "       f.sum_value     as count_value, " +
                "       f.average_value as stat_value, " +
                "       %4$d as cohort_definition_id, " +
                "       %5$d as cohort_characterization_generation_id " +
                " from features f " +
                "       join feature_refs fr on fr.covariate_id = f.covariate_id and fr.cohort_definition_id = f.cohort_definition_id " +
                "       JOIN analysis_refs ar " +
                "         on ar.analysis_id = fr.analysis_id and ar.cohort_definition_id = fr.cohort_definition_id " +
                "       LEFT JOIN @cdm_database_schema.concept c on c.concept_id = fr.concept_id;";
        
        final String distributionRetrievingQuery = "\n" +
                " with " +
                "     features as (%1$s), " +
                "     feature_refs as (%2$s), " +
                "     analysis_refs as (%3$s) " +
                " insert into @results_database_schema.cc_results " +
                "    (type, covariate_id, covariate_name, analysis_id, analysis_name, concept_id, " +
                "     count_value, min_value, max_value, avg_value, stdev_value, median_value, " +
                "     p10_value, p25_value, p75_value, p90_value, cohort_definition_id, cohort_characterization_generation_id) " +
                " select 'DISTRIBUTION', " +
                "       f.covariate_id, " +
                "       fr.covariate_name, " +
                "       ar.analysis_id, " +
                "       ar.analysis_name, " +
                "       fr.concept_id, " +
                "       f.count_value, " +
                "       f.min_value, " +
                "       f.max_value, " +
                "       f.average_value, " +
                "       f.standard_deviation, " +
                "       f.median_value, " +
                "       f.p10_value, " +
                "       f.p25_value, " +
                "       f.p75_value, " +
                "       f.p90_value, " +
                "       %4$d as cohort_definition_id, " +
                "       %5$d as cohort_characterization_generation_id " +
                " from features f " +
                "       join feature_refs fr on fr.covariate_id = f.covariate_id and fr.cohort_definition_id = f.cohort_definition_id " +
                "       JOIN analysis_refs ar " +
                "         on ar.analysis_id = fr.analysis_id and ar.cohort_definition_id = fr.cohort_definition_id " +
                "       LEFT JOIN @cdm_database_schema.concept c on c.concept_id = fr.concept_id;";
        
        final String customDistributionQueryWrapper = "\n" +
                "insert into " +
                " @results_database_schema.cc_results (type, " +
                "     covariate_id, " +
                "     covariate_name, " +
                "     analysis_id, " +
                "     analysis_name, " +
                "     concept_id, " +
                "     count_value, " +
                "     min_value, " +
                "     max_value, " +
                "     avg_value, " +
                "     stdev_value, " +
                "     median_value, " +
                "     p10_value, " +
                "     p25_value, " +
                "     p75_value, " +
                "     p90_value, " +
                "     cohort_definition_id, " +
                "     cohort_characterization_generation_id) " +
                " select 'DISTRIBUTION', " +
                "        covariate_id, " +
                "        covariate_name, " +
                "        %1$d as analysis_id, " +
                "        %2$s as analysis_name, " +
                "        concept_id, " +
                "        count_value, " +
                "        min_value, " +
                "        max_value, " +
                "        average_value, " +
                "        standard_deviation, " +
                "        median_value, " +
                "        p10_value, " +
                "        p25_value, " +
                "        p75_value, " +
                "        p90_value, " +
                "        %3$d as cohort_definition_id, " +
                "        %4$d as cohort_characterization_generation_id " +
                " from (%5$s) subquery;";
        
        final String customPrevalenceQueryWrapper = "\n" +
                "insert into @results_database_schema.cc_results ( " +
                "     type, " +
                "     covariate_id, " +
                "     covariate_name, " +
                "     analysis_id, " +
                "     analysis_name, " +
                "     concept_id, " +
                "     count_value, " +
                "     avg_value, " +
                "     cohort_definition_id, " +
                "     cohort_characterization_generation_id) " +
                "select 'PREVALENCE'    as type, " +
                "        covariate_id, " +
                "        covariate_name, " +
                "        %1$d as analysis_id, " +
                "        %2$s as analysis_name, " +
                "        concept_id, " +
                "        sum_value       as count_value, " +
                "        average_value   as stat_value, " +
                "        %3$d            as cohort_definition_id, " +
                "        %4$d            as cohort_characterization_generation_id " +
                "from (%5$s) subquery;";
        
        final CohortCharacterizationEntity cohortCharacterization;
        final String resultSchema;
        final String cdmSchema;
        
        private final Map<String, Object> jobParams;
        final String targetDialect;
        
        private final Long jobId;

        CcTask(final ChunkContext context) {
            this.jobParams = context.getStepContext().getJobParameters();
            this.targetDialect = jobParams.get(TARGET_DIALECT).toString();
            this.cohortCharacterization = ccService.findByIdWithLinkedEntities(
                    Long.valueOf(jobParams.get(COHORT_CHARACTERIZATION_ID).toString())
            );
            this.resultSchema = jobParams.get(RESULTS_DATABASE_SCHEMA).toString();
            this.cdmSchema = jobParams.get(CDM_DATABASE_SCHEMA).toString();
            
            this.jobId = context.getStepContext().getStepExecution().getJobExecution().getId();
        }
        
        private void run() {
            cohortCharacterization.getCohortDefinitions()
                    .forEach(definition -> runAnalysisOnCohort(definition.getId()));
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
        
        private List<String> getQueriesForCustomDistributionAnalyses(final Integer cohortId) {
            return cohortCharacterization.getFeatureAnalyses()
                    .stream()
                    .filter(FeAnalysisEntity::isCustom)
                    .filter(v -> v.getStatType() == CcResultType.DISTRIBUTION)
                    .map(v -> String.format(customDistributionQueryWrapper, v.getId(), org.springframework.util.StringUtils.quote(v.getName()), cohortId, jobId, v.getDesign()))
                    .collect(Collectors.toList());
        }
        
        private List<String> getQueriesForCustomPrevalenceAnalyses(final Integer cohortId) {
            return cohortCharacterization.getFeatureAnalyses()
                    .stream()
                    .filter(FeAnalysisEntity::isCustom)
                    .filter(v -> v.getStatType() == CcResultType.PREVALENCE)
                    .map(v -> String.format(customPrevalenceQueryWrapper, v.getId(), org.springframework.util.StringUtils.quote(v.getName()), cohortId, jobId, v.getDesign()))
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
                queries.add(String.format(distributionRetrievingQuery, distFeatures, featureRefs, analysisRefs, cohortId, jobId));
            }
            if (ccHasPresetPrevalenceAnalyses()) {
                final String featureColumns = "cohort_definition_id, covariate_id, sum_value, average_value";
                final String features = String.format(cohortWrapper, cohortId, featureColumns,
                        StringUtils.stripEnd(jsonObject.getString("sqlQueryFeatures"), ";"));
                queries.add(String.format(prevalenceRetrievingQuery, features, featureRefs, analysisRefs, cohortId, jobId));
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
            options.cdmSchema = jobParams.get(CDM_DATABASE_SCHEMA).toString();
            options.resultSchema = this.resultSchema;
            options.cohortId = id;
            return options;
        }

        private String[] getSqlQueriesToRun(final JSONObject jsonObject, final Integer cohortDefinitionId) {
            final StringJoiner joiner = new StringJoiner("\r  ---- \r ");
            
            joiner.add(jsonObject.getString("sqlConstruction"));
            
            getQueriesForResultsRetrieving(jsonObject,cohortDefinitionId).forEach(joiner::add);
            getQueriesForCustomDistributionAnalyses(cohortDefinitionId).forEach(joiner::add);
            getQueriesForCustomPrevalenceAnalyses(cohortDefinitionId).forEach(joiner::add);
            
            joiner.add(jsonObject.getString("sqlCleanup"));
            
            final String sql = SqlRender.renderSql(joiner.toString(),
                    new String[]{RESULTS_DATABASE_SCHEMA, CDM_DATABASE_SCHEMA},
                    new String[]{resultSchema, cdmSchema});
            final String translatedSql = SqlTranslate.translateSql(sql, targetDialect, SessionUtils.sessionId(), null);
            return SqlSplit.splitSql(translatedSql);
        }

        private JSONObject createFeJsonObject(final CohortExpressionQueryBuilder.BuildExpressionQueryOptions options) {
            FeatureExtraction.init(null);
            String settings = buildSettings();
            String sqlJson = FeatureExtraction.createSql(settings, true, options.resultSchema + ".cohort",
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
