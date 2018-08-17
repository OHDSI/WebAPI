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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder;
import org.ohdsi.featureExtraction.FeatureExtraction;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
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
    
    
    public GenerateCohortCharacterizationTasklet(
            final JdbcTemplate jdbcTemplate, 
            final TransactionTemplate transactionTemplate,
            final CcService ccService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
        this.ccService = ccService;
        
        this.taskExecutor = Executors.newSingleThreadExecutor();
    }

    private int[] doTask(ChunkContext chunkContext) {
        initTx();
        new CcRunner(chunkContext).run();
        return null;
    }

    private void initTx() {
        DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition();
        txDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus initStatus = this.transactionTemplate.getTransactionManager().getTransaction(txDefinition);
        this.transactionTemplate.getTransactionManager().commit(initStatus);
    }
    
    private class CcRunner {

        final String prevalenceRetrievingQuery = "with " +
                "     features as (%1$s), " +
                "     feature_refs as (%2$s), " +
                "     analysis_refs as(%3$s) " +
                "insert into @results_database_schema.cohort_characterization_results " +
                "(type, covariate_id, covariate_name, analysis_id, analysis_name, concept_id, count_value, avg_value, cohort_definition_id, cohort_characterization_generation_id) " +
                "select 'PREVALENCE' as type, " +
                "       f.covariate_id, " +
                "       fr.covariate_name, " +
                "       ar.analysis_id, " +
                "       ar.analysis_name, " +
                "       fr.concept_id, " +
                "       f.sum_value     as count_value, " +
                "       f.average_value as stat_value, " +
                "       %4$d as cohort_definition_id, " +
                "       %5$d as cohort_characterization_generation_id " +
                "from features f " +
                "       join feature_refs fr on fr.covariate_id = f.covariate_id and fr.cohort_definition_id = f.cohort_definition_id " +
                "       JOIN analysis_refs ar " +
                "         on ar.analysis_id = fr.analysis_id and ar.cohort_definition_id = fr.cohort_definition_id " +
                "       LEFT JOIN @cdm_database_schema.concept c on c.concept_id = fr.concept_id;";
        
        final String distributionRetrievingQuery = "with " +
                "     features as (%1$s),\n" +
                "     feature_refs as (%2$s),\n" +
                "     analysis_refs as (%3$s)\n" +
                "insert into five_three_plus_results.cohort_characterization_results\n" +
                "    (type, covariate_id, covariate_name, analysis_id, analysis_name, concept_id,\n" +
                "     count_value, min_value, max_value, avg_value, stdev_value, median_value,\n" +
                "     p10_value, p25_value, p75_value, p90_value, cohort_definition_id, cohort_characterization_generation_id)\n" +
                "select 'DISTRIBUTION',\n" +
                "       f.covariate_id,\n" +
                "       fr.covariate_name,\n" +
                "       ar.analysis_id,\n" +
                "       ar.analysis_name,\n" +
                "       fr.concept_id,\n" +
                "       f.count_value,\n" +
                "       f.min_value,\n" +
                "       f.max_value,\n" +
                "       f.average_value,\n" +
                "       f.standard_deviation,\n" +
                "       f.median_value,\n" +
                "       f.p10_value,\n" +
                "       f.p25_value,\n" +
                "       f.p75_value,\n" +
                "       f.p90_value,\n" +
                "       %4$d as cohort_definition_id, " +
                "       %5$d as cohort_characterization_generation_id " +
                "from features f\n" +
                "       join feature_refs fr on fr.covariate_id = f.covariate_id and fr.cohort_definition_id = f.cohort_definition_id\n" +
                "       JOIN analysis_refs ar\n" +
                "         on ar.analysis_id = fr.analysis_id and ar.cohort_definition_id = fr.cohort_definition_id\n" +
                "       LEFT JOIN five_three_plus.concept c on c.concept_id = fr.concept_id;";
        
        final CohortCharacterizationEntity cohortCharacterization;
        final String resultSchema;
        final String cdmSchema;
        
        private final Map<String, Object> jobParams;
        final String targetDialect;
        
        private final Long jobId;

        CcRunner(final ChunkContext context) {
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
                    .forEach(definition -> runSql(definition.getId()));
        }

        private int[] runSql(final Integer cohortDefinitionId) {
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
        
        private List<String> retrieveResults(final JSONObject jsonObject, final Integer cohortId) {
            final String cohortWrapper = "select %1$d as %2$s from (%3$s) W";
            
            final String featureRefColumns = "cohort_definition_id, covariate_id, covariate_name, analysis_id, concept_id";
            final String featureRefs = String.format(cohortWrapper, cohortId, featureRefColumns,
                    StringUtils.stripEnd(jsonObject.getString("sqlQueryFeatureRef"), ";"));
            
            final String analysisRefColumns = "cohort_definition_id, CAST(analysis_id AS INT) analysis_id, analysis_name, domain_id, start_day, end_day, CAST(is_binary AS CHAR(1)) is_binary,CAST(missing_means_zero AS CHAR(1)) missing_means_zero";
            final String analysisRefs = String.format(cohortWrapper, cohortId, analysisRefColumns,
                    StringUtils.stripEnd(jsonObject.getString("sqlQueryAnalysisRef"), ";"));
            
            final List<String> queries = new ArrayList<>();
            
            if (ccHasDistributionAnalyses()) {
                final String distColumns = "cohort_definition_id, covariate_id, count_value, min_value, max_value, average_value, "
                        + "standard_deviation, median_value, p10_value, p25_value, p75_value, p90_value";
                final String distFeatures = String.format(cohortWrapper, cohortId, distColumns,
                        StringUtils.stripEnd(jsonObject.getString("sqlQueryContinuousFeatures"), ";"));
                queries.add(String.format(distributionRetrievingQuery, distFeatures, featureRefs, analysisRefs, cohortCharacterization.getId(), jobId));
            }
            if (ccHasPrevalenceAnalyses()) {
                final String featureColumns = "cohort_definition_id, covariate_id, sum_value, average_value";
                final String features = String.format(cohortWrapper, cohortId, featureColumns,
                        StringUtils.stripEnd(jsonObject.getString("sqlQueryFeatures"), ";"));
                queries.add(String.format(prevalenceRetrievingQuery, features, featureRefs, analysisRefs, cohortCharacterization.getId(), jobId));
            }
            
            return queries;
        }
        
        private boolean ccHasPrevalenceAnalyses() {
            return cohortCharacterization.getFeatureAnalyses()
                    .stream()
                    .anyMatch(analysis -> analysis.getStatType() == CcResultType.PREVALENCE);
        }
        
        private boolean ccHasDistributionAnalyses() {
            return cohortCharacterization.getFeatureAnalyses()
                    .stream()
                    .anyMatch(analysis -> analysis.getStatType() == CcResultType.DISTRIBUTION);
        }
        
        private CohortExpressionQueryBuilder.BuildExpressionQueryOptions createDefaultOptions(final Integer id) {
            final CohortExpressionQueryBuilder.BuildExpressionQueryOptions options = new CohortExpressionQueryBuilder.BuildExpressionQueryOptions();
            options.cdmSchema = jobParams.get(CDM_DATABASE_SCHEMA).toString();
            options.resultSchema = this.resultSchema;
            options.cohortId = id;
            return options;
        }

        private String[] getSqlQueriesToRun(final JSONObject jsonObject, final Integer cohortDefinitionId) {
            final StringJoiner joiner = new StringJoiner("\r\n ---- \r\n");
            
            joiner.add(jsonObject.getString("sqlConstruction"));
            retrieveResults(jsonObject,cohortDefinitionId).forEach(joiner::add);
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
            StringWriter stringWriter = new StringWriter();
            JSONWriter jsonWriter = new JSONWriter(stringWriter);
            jsonWriter.object();
            jsonWriter.key("temporal");
            jsonWriter.value(Boolean.FALSE);
            cohortCharacterization.getParameters().forEach(param -> {
                jsonWriter.key(param.getName());
                jsonWriter.value(param.getValue());
            });
            cohortCharacterization.getFeatureAnalyses()
                    .stream()
                    .filter(FeAnalysisEntity::isPreset)
                    .forEach(analysis -> {
                        jsonWriter.key(analysis.getDesign());
                        jsonWriter.value(Boolean.TRUE);
                    });
            jsonWriter.endObject();
            return stringWriter.toString();
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
