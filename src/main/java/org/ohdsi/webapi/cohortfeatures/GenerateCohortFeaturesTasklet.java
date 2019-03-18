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
package org.ohdsi.webapi.cohortfeatures;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder;
import org.ohdsi.featureExtraction.FeatureExtraction;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.util.JobUtils;
import org.ohdsi.webapi.util.SessionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

import static org.ohdsi.webapi.Constants.Params.CDM_DATABASE_SCHEMA;
import static org.ohdsi.webapi.Constants.Params.RESULTS_DATABASE_SCHEMA;
import static org.ohdsi.webapi.Constants.Params.TEMP_DATABASE_SCHEMA;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 * @author Ajit Londhe <alondhe2@its.jnj.com>
 */
public class GenerateCohortFeaturesTasklet implements Tasklet 
{
    private static final Logger log = LoggerFactory.getLogger(GenerateCohortFeaturesTasklet.class);
    private final TransactionTemplate transactionTemplate;

		private final ExecutorService taskExecutor;
		private boolean stopped = false;
		private long checkInterval = 1000;

    private final JdbcTemplate jdbcTemplate;
   
    public GenerateCohortFeaturesTasklet(final JdbcTemplate jdbcTemplate, final TransactionTemplate transactionTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
				taskExecutor = Executors.newSingleThreadExecutor();
				
    }
    
    private String getSql(CohortExpressionQueryBuilder.BuildExpressionQueryOptions options, 
            JSONObject jsonObject)
    {
        String insertStr = "insert into @resultsDatabaseSchema.%1$s \r\n %2$s;";
        String cohortWrapper = "select %1$d as %2$s from (%3$s) W";
        StringJoiner joiner = new StringJoiner("\r\n ---- \r\n");
        joiner.add(jsonObject.getString("sqlConstruction"));
        
        String columns = "cohort_definition_id, covariate_id, sum_value, average_value";
        
        joiner.add(String.format(insertStr, 
                "cohort_features", 
                String.format(cohortWrapper, options.cohortId, columns, 
                        StringUtils.stripEnd(jsonObject.getString("sqlQueryFeatures"), ";"))));

        columns = "cohort_definition_id, covariate_id, count_value, min_value, max_value, average_value, "
                        + "standard_deviation, median_value, p10_value, p25_value, p75_value, p90_value";
        joiner.add(String.format(insertStr, 
                "cohort_features_dist", 
                String.format(cohortWrapper, options.cohortId, columns, 
                        StringUtils.stripEnd(jsonObject.getString("sqlQueryContinuousFeatures"), ";"))));

        columns = "cohort_definition_id, covariate_id, covariate_name, analysis_id, concept_id";
        joiner.add(String.format(insertStr, 
                "cohort_features_ref", 
                String.format(cohortWrapper, options.cohortId, columns, 
                        StringUtils.stripEnd(jsonObject.getString("sqlQueryFeatureRef"), ";"))));

        columns = "cohort_definition_id, CAST(analysis_id AS INT) analysis_id, analysis_name, domain_id, start_day, end_day, CAST(is_binary AS CHAR(1)) is_binary,CAST(missing_means_zero AS CHAR(1)) missing_means_zero";
        joiner.add(String.format(insertStr, 
                "cohort_features_analysis_ref", 
                String.format(cohortWrapper, options.cohortId, columns, 
                        StringUtils.stripEnd(jsonObject.getString("sqlQueryAnalysisRef"), ";"))));

        joiner.add(jsonObject.getString("sqlCleanup"));

        return SqlRender.renderSql(joiner.toString(), 
                new String[]{"resultsDatabaseSchema"}, 
                new String[]{options.resultSchema});
    }
    
    private int[] doTask(ChunkContext chunkContext)
    {
      int[] result = null;

      Map<String, Object> jobParams = chunkContext.getStepContext().getJobParameters();
      Integer defId = Integer.valueOf(jobParams.get("cohort_definition_id").toString());
      String sessionId = SessionUtils.sessionId();

      try {
        DefaultTransactionDefinition requresNewTx = new DefaultTransactionDefinition();
        requresNewTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        TransactionStatus initStatus = this.transactionTemplate.getTransactionManager().getTransaction(requresNewTx);
        this.transactionTemplate.getTransactionManager().commit(initStatus);
        
        CohortExpressionQueryBuilder.BuildExpressionQueryOptions options = new CohortExpressionQueryBuilder.BuildExpressionQueryOptions();
        options.cohortId = defId;
        options.cdmSchema = JobUtils.getSchema(jobParams, CDM_DATABASE_SCHEMA);
        options.resultSchema = JobUtils.getSchema(jobParams, RESULTS_DATABASE_SCHEMA);
        final String tempSchema = JobUtils.getSchema(jobParams, TEMP_DATABASE_SCHEMA);

        List<String> tableNames = ImmutableList.of("cohort_features", "cohort_features_dist", "cohort_features_ref", "cohort_features_analysis_ref");

        String deleteSql = tableNames.stream().map(tableName -> SqlTranslate.translateSql(
                  String.format("DELETE FROM %1$s.%2$s WHERE cohort_definition_id = %3$d;",
                          options.resultSchema, tableName, options.cohortId),
                  jobParams.get("target_dialect").toString(), sessionId, tempSchema)).collect(Collectors.joining());

        this.jdbcTemplate.batchUpdate(deleteSql.split(";")); // use batch update since SQL translation may produce multiple statements

        FeatureExtraction.init(null);
        String settings = FeatureExtraction.getDefaultPrespecAnalyses();
        String sqlJson = FeatureExtraction.createSql(settings, true, options.resultSchema + ".cohort", 
          "subject_id", options.cohortId, options.cdmSchema);

        JSONObject jsonObject = new JSONObject(sqlJson);

        String sql = getSql(options, jsonObject);
        String translatedSql = SqlTranslate.translateSql(sql, jobParams.get("target_dialect").toString(), sessionId, tempSchema);
        String[] sqlStatements = SqlSplit.splitSql(translatedSql);
				FutureTask<int[]> batchUpdateTask = new FutureTask<>(() -> GenerateCohortFeaturesTasklet.this.jdbcTemplate.batchUpdate(sqlStatements));
				taskExecutor.execute(batchUpdateTask);
				while(true) {
					Thread.sleep(checkInterval);
					if (batchUpdateTask.isDone()) {
						result = batchUpdateTask.get();
						break;
					} else if (stopped) {
						batchUpdateTask.cancel(true);
						break;
					}
				}
				taskExecutor.shutdown();
      } catch (Exception e) {
        log.error("Failed to generate cohort: {}", defId, e);
        throw new RuntimeException(e);
      }
      return result;
    }

  @Override
  public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
    try {
      final int[] ret = this.transactionTemplate.execute(new TransactionCallback<int[]>() {

        @Override
        public int[] doInTransaction(final TransactionStatus status) {
          return doTask(chunkContext);
        }
      });
    } catch (final TransactionException e) {
      log.error(e.getMessage(), e);
      throw e;//FAIL job status
    }
    return RepeatStatus.FINISHED;
  }
}
