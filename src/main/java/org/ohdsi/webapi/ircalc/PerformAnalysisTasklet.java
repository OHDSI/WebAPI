/*
 * Copyright 2015 Observational Health Data Sciences and Informatics [OHDSI.org].
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
package org.ohdsi.webapi.ircalc;

import static org.ohdsi.webapi.util.SecurityUtils.whitelist;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.GenerationStatus;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
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

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
public class PerformAnalysisTasklet implements Tasklet {

  private static final Log log = LogFactory.getLog(PerformAnalysisTasklet.class);
  private static final int MAX_MESSAGE_LENGTH = 2000;
  
  private final static IRAnalysisQueryBuilder analysisQueryBuilder = new IRAnalysisQueryBuilder();

  private final JdbcTemplate jdbcTemplate;
  private final TransactionTemplate transactionTemplate;
  private final IncidenceRateAnalysisRepository incidenceRateAnalysisRepository;

  public PerformAnalysisTasklet(
          final JdbcTemplate jdbcTemplate, 
          final TransactionTemplate transactionTemplate,
          final IncidenceRateAnalysisRepository incidenceRateAnalysisRepository) {
    this.jdbcTemplate = jdbcTemplate;
    this.transactionTemplate = transactionTemplate;
    this.incidenceRateAnalysisRepository = incidenceRateAnalysisRepository;
  }

  private ExecutionInfo findExecutionInfoBySourceId(Collection<ExecutionInfo> infoList, Integer sourceId)
  {
    for (ExecutionInfo info : infoList) {
      if (sourceId.equals(info.getId().getSourceId()))
        return info;
    }
    return null;
  }
  
  private int[] doTask(ChunkContext chunkContext) {
    ObjectMapper mapper = new ObjectMapper();
    
    Map<String, Object> jobParams = chunkContext.getStepContext().getJobParameters();
    Integer analysisId = Integer.valueOf(jobParams.get("analysis_id").toString());
    int[] result;
    try {
      String sessionId = SessionUtils.sessionId();
      IncidenceRateAnalysis analysis = this.incidenceRateAnalysisRepository.findOne(analysisId);
      IncidenceRateAnalysisExpression expression = mapper.readValue(analysis.getDetails().getExpression(), IncidenceRateAnalysisExpression.class);
      
      IRAnalysisQueryBuilder.BuildExpressionQueryOptions options = new IRAnalysisQueryBuilder.BuildExpressionQueryOptions();
      options.cdmSchema = jobParams.get("cdm_database_schema").toString();
      options.resultsSchema = jobParams.get("results_database_schema").toString();
      options.vocabularySchema = jobParams.get("vocabulary_database_schema").toString();

      String delete = "DELETE FROM @tableQualifier.ir_strata WHERE analysis_id = @analysis_id";
      PreparedStatementRenderer psr = new PreparedStatementRenderer(null, delete, "tableQualifier",
        options.resultsSchema, "analysis_id", analysisId);
      jdbcTemplate.update(psr.getSql(), psr.getSetter());

      String insert = "INSERT INTO @results_schema.ir_strata (analysis_id, strata_sequence, name, description) VALUES (@analysis_id,@strata_sequence,@name,@description)";

      String [] params = {"analysis_id", "strata_sequence", "name", "description"};
      List<StratifyRule> strataRules = expression.strata;
      for (int i = 0; i< strataRules.size(); i++)
      {
        StratifyRule r = strataRules.get(i);
        psr = new PreparedStatementRenderer(null, insert, "results_schema",
          options.resultsSchema, params, new Object[] { analysisId, i, r.name, r.description});
        psr.setTargetDialect(jobParams.get("target_dialect").toString());
        jdbcTemplate.update(psr.getSql(), psr.getSetter());
      }
      
      String expressionSql = analysisQueryBuilder.buildAnalysisQuery(analysis, options);
      String translatedSql = SqlTranslate.translateSql(expressionSql, jobParams.get("target_dialect").toString(), sessionId, null);
      String[] sqlStatements = SqlSplit.splitSql(translatedSql);
      result = PerformAnalysisTasklet.this.jdbcTemplate.batchUpdate(sqlStatements);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  @Override
  public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
    Date startTime = Calendar.getInstance().getTime();
    Map<String, Object> jobParams = chunkContext.getStepContext().getJobParameters();
    Integer analysisId = Integer.valueOf(jobParams.get("analysis_id").toString());
    Integer sourceId = Integer.valueOf(jobParams.get("source_id").toString());
    boolean isValid = false;
    String statusMessage = "OK";

    DefaultTransactionDefinition requresNewTx = new DefaultTransactionDefinition();
    requresNewTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    
    TransactionStatus initStatus = this.transactionTemplate.getTransactionManager().getTransaction(requresNewTx);
    IncidenceRateAnalysis analysis = this.incidenceRateAnalysisRepository.findOne(analysisId);
    
    ExecutionInfo analysisInfo = findExecutionInfoBySourceId(analysis.getExecutionInfoList(), sourceId);
    analysisInfo.setIsValid(false);
    analysisInfo.setStartTime(startTime);
    analysisInfo.setStatus(GenerationStatus.RUNNING);
    
    this.incidenceRateAnalysisRepository.save(analysis);
    this.transactionTemplate.getTransactionManager().commit(initStatus);
    
    try {
      final int[] ret = this.transactionTemplate.execute(new TransactionCallback<int[]>() {

        @Override
        public int[] doInTransaction(final TransactionStatus status) {
          return doTask(chunkContext);
        }
      });
      log.debug("Update count: " + ret.length);
      isValid = true;
    } catch (final Exception e) {
      isValid = false;
      statusMessage = e.getMessage();
      log.error(e.getMessage(), e);
      throw e;//FAIL job status
    }
    finally {
      TransactionStatus completeStatus = this.transactionTemplate.getTransactionManager().getTransaction(requresNewTx);
      Date endTime = Calendar.getInstance().getTime();
      analysis = this.incidenceRateAnalysisRepository.findOne(analysisId);
      
      analysisInfo = findExecutionInfoBySourceId(analysis.getExecutionInfoList(), sourceId);
      analysisInfo.setIsValid(isValid);
      analysisInfo.setExecutionDuration((int)(endTime.getTime() - startTime.getTime()));
      analysisInfo.setStatus(GenerationStatus.COMPLETE);
      analysisInfo.setMessage(statusMessage.substring(0, Math.min(MAX_MESSAGE_LENGTH, statusMessage.length())));
      
      this.incidenceRateAnalysisRepository.save(analysis);
      this.transactionTemplate.getTransactionManager().commit(completeStatus);
    }

    return RepeatStatus.FINISHED;
  }

}