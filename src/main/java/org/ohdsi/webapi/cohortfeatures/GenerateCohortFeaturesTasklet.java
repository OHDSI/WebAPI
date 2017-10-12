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

import org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.util.SessionUtils;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;
import java.util.StringJoiner;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.sql.SqlRender;
import org.springframework.transaction.TransactionException;

import org.json.JSONObject;
import org.ohdsi.featureExtraction.FeatureExtraction;
import org.ohdsi.sql.SqlSplit;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 * @author Ajit Londhe <alondhe2@its.jnj.com>
 */
public class GenerateCohortFeaturesTasklet implements Tasklet 
{
    private static final Log log = LogFactory.getLog(GenerateCohortFeaturesTasklet.class);
    private final TransactionTemplate transactionTemplate;
    //private final static CohortExpressionQueryBuilder expressionQueryBuilder = new CohortExpressionQueryBuilder();

    private final JdbcTemplate jdbcTemplate;
   
    public GenerateCohortFeaturesTasklet(final JdbcTemplate jdbcTemplate, final TransactionTemplate transactionTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
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

        columns = "cohort_definition_id, analysis_id, analysis_name, domain_id, start_day, end_day, is_binary, missing_means_zero";
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

      try
      {
        DefaultTransactionDefinition requresNewTx = new DefaultTransactionDefinition();
        requresNewTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        TransactionStatus initStatus = this.transactionTemplate.getTransactionManager().getTransaction(requresNewTx);
        this.transactionTemplate.getTransactionManager().commit(initStatus);
        
        CohortExpressionQueryBuilder.BuildExpressionQueryOptions options = new CohortExpressionQueryBuilder.BuildExpressionQueryOptions();
        options.cohortId = defId;
        options.cdmSchema = jobParams.get("cdm_database_schema").toString();
        options.resultSchema = jobParams.get("results_database_schema").toString();

        String deleteSql = "";
        String[] tableNames = new String[] { "cohort_features", "cohort_features_dist", "cohort_features_ref", "cohort_features_analysis_ref"};
        
        for (String tableName : tableNames)
        {
            deleteSql += SqlTranslate.translateSql(
                    String.format("DELETE FROM %1$s.%2$s WHERE cohort_definition_id = %3$d;", 
                      options.resultSchema, tableName, options.cohortId), 
                    jobParams.get("target_dialect").toString(), sessionId, null);
        }
        
        this.jdbcTemplate.batchUpdate(deleteSql.split(";")); // use batch update since SQL translation may produce multiple statements

        FeatureExtraction.init(null);
        String settings = FeatureExtraction.getDefaultPrespecAnalyses();
        String sqlJson = FeatureExtraction.createSql(settings, true, options.resultSchema + ".cohort", 
          "subject_id", options.cohortId, options.cdmSchema);

        JSONObject jsonObject = new JSONObject(sqlJson);

        String sql = getSql(options, jsonObject);
        String translatedSql = SqlTranslate.translateSql(sql, jobParams.get("target_dialect").toString(), sessionId, null);
        String[] sqlStatements = SqlSplit.splitSql(translatedSql);
        this.jdbcTemplate.batchUpdate(sqlStatements);
      } 
      catch (Exception e) 
      {
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
