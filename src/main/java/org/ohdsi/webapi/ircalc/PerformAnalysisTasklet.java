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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.LogFactory;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.GenerationStatus;
import org.ohdsi.webapi.common.generation.CancelableTasklet;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.util.CancelableJdbcTemplate;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.ohdsi.webapi.util.SessionUtils;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
public class PerformAnalysisTasklet extends CancelableTasklet {

  private static final int MAX_MESSAGE_LENGTH = 2000;
  
  private final static IRAnalysisQueryBuilder analysisQueryBuilder = new IRAnalysisQueryBuilder();

  private final TransactionTemplate transactionTemplate;
  private final IncidenceRateAnalysisRepository incidenceRateAnalysisRepository;
  private ExecutionInfo analysisInfo;
  private Date startTime;

  public PerformAnalysisTasklet(
          final CancelableJdbcTemplate jdbcTemplate,
          final TransactionTemplate transactionTemplate,
          final IncidenceRateAnalysisRepository incidenceRateAnalysisRepository) {

    super(LogFactory.getLog(PerformAnalysisTasklet.class), jdbcTemplate, transactionTemplate);
    this.transactionTemplate = transactionTemplate;
    this.incidenceRateAnalysisRepository = incidenceRateAnalysisRepository;
  }

  private Optional<ExecutionInfo> findExecutionInfoBySourceId(Collection<ExecutionInfo> infoList, Integer sourceId)
  {
    return infoList.stream()
            .filter(info -> Objects.equals(info.getId().getSourceId(), sourceId))
            .findFirst();
  }
  
  protected String[] prepareQueries(ChunkContext chunkContext, CancelableJdbcTemplate jdbcTemplate) {
    ObjectMapper mapper = new ObjectMapper();
    
    Map<String, Object> jobParams = chunkContext.getStepContext().getJobParameters();

    Source source = new Source();
    source.setSourceDialect(jobParams.get("target_dialect").toString());

    Integer analysisId = Integer.valueOf(jobParams.get("analysis_id").toString());
    try {
      String sessionId = SessionUtils.sessionId();
      IncidenceRateAnalysis analysis = this.incidenceRateAnalysisRepository.findOne(analysisId);
      IncidenceRateAnalysisExpression expression = mapper.readValue(analysis.getDetails().getExpression(), IncidenceRateAnalysisExpression.class);
      
      IRAnalysisQueryBuilder.BuildExpressionQueryOptions options = new IRAnalysisQueryBuilder.BuildExpressionQueryOptions();
      options.cdmSchema = jobParams.get("cdm_database_schema").toString();
      options.resultsSchema = jobParams.get("results_database_schema").toString();
      options.vocabularySchema = jobParams.get("vocabulary_database_schema").toString();

      String delete = "DELETE FROM @tableQualifier.ir_strata WHERE analysis_id = @analysis_id;";
      PreparedStatementRenderer psr = new PreparedStatementRenderer(source, delete, "tableQualifier",
        options.resultsSchema, "analysis_id", analysisId);
      jdbcTemplate.update(psr.getSql(), psr.getSetter());

      String insert = "INSERT INTO @results_schema.ir_strata (analysis_id, strata_sequence, name, description) VALUES (@analysis_id,@strata_sequence,@name,@description)";

      String [] params = {"analysis_id", "strata_sequence", "name", "description"};
      List<StratifyRule> strataRules = expression.strata;
      for (int i = 0; i< strataRules.size(); i++)
      {
        StratifyRule r = strataRules.get(i);
        psr = new PreparedStatementRenderer(source, insert, "results_schema",
          options.resultsSchema, params, new Object[] { analysisId, i, r.name, r.description});
        jdbcTemplate.update(psr.getSql(), psr.getSetter());
      }
      
      String expressionSql = analysisQueryBuilder.buildAnalysisQuery(analysis, options);
      String translatedSql = SqlTranslate.translateSql(expressionSql, jobParams.get("target_dialect").toString(), sessionId, null);
      return SqlSplit.splitSql(translatedSql);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void doBefore(ChunkContext chunkContext) {
    startTime = Calendar.getInstance().getTime();
    Map<String, Object> jobParams = chunkContext.getStepContext().getJobParameters();
    Integer analysisId = Integer.valueOf(jobParams.get("analysis_id").toString());
    Integer sourceId = Integer.valueOf(jobParams.get("source_id").toString());

    DefaultTransactionDefinition requresNewTx = new DefaultTransactionDefinition();
    requresNewTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

    TransactionStatus initStatus = this.transactionTemplate.getTransactionManager().getTransaction(requresNewTx);
    IncidenceRateAnalysis analysis = this.incidenceRateAnalysisRepository.findOne(analysisId);

    findExecutionInfoBySourceId(analysis.getExecutionInfoList(), sourceId).ifPresent(analysisInfo -> {
      analysisInfo.setIsValid(false);
      analysisInfo.setStartTime(startTime);
      analysisInfo.setStatus(GenerationStatus.RUNNING);
    });

    this.incidenceRateAnalysisRepository.save(analysis);
    this.transactionTemplate.getTransactionManager().commit(initStatus);
  }

  @Override
  protected void doAfter(StepContribution contribution, ChunkContext chunkContext) {

    super.doAfter(contribution, chunkContext);
    boolean isValid = !Constants.FAILED.equals(contribution.getExitStatus().getExitCode());
    String statusMessage = contribution.getExitStatus().getExitDescription();

    Map<String, Object> jobParams = chunkContext.getStepContext().getJobParameters();
    Integer analysisId = Integer.valueOf(jobParams.get("analysis_id").toString());
    Integer sourceId = Integer.valueOf(jobParams.get("source_id").toString());

    DefaultTransactionDefinition requresNewTx = new DefaultTransactionDefinition();
    requresNewTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    TransactionStatus completeStatus = this.transactionTemplate.getTransactionManager().getTransaction(requresNewTx);
    Date endTime = Calendar.getInstance().getTime();
    IncidenceRateAnalysis analysis = this.incidenceRateAnalysisRepository.findOne(analysisId);

    findExecutionInfoBySourceId(analysis.getExecutionInfoList(), sourceId).ifPresent(analysisInfo -> {
      analysisInfo.setIsValid(isValid);
      analysisInfo.setCanceled(Objects.equals(Constants.CANCELED, contribution.getExitStatus().getExitCode()));
      analysisInfo.setExecutionDuration((int) (endTime.getTime() - startTime.getTime()));
      analysisInfo.setStatus(GenerationStatus.COMPLETE);
      analysisInfo.setMessage(statusMessage.substring(0, Math.min(MAX_MESSAGE_LENGTH, statusMessage.length())));
    });

    this.incidenceRateAnalysisRepository.save(analysis);
    this.transactionTemplate.getTransactionManager().commit(completeStatus);
  }

}