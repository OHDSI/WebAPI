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
package org.ohdsi.webapi.feasibility;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetails;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.CohortExpression;
import org.ohdsi.webapi.cohortdefinition.CohortGenerationInfo;
import org.ohdsi.webapi.cohortdefinition.CriteriaGroup;
import org.ohdsi.webapi.cohortdefinition.ExpressionType;
import org.ohdsi.webapi.cohortdefinition.GenerationStatus;
import org.ohdsi.webapi.util.SessionUtils;
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

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
public class PerformFeasibilityTasklet implements Tasklet {

  private static final Log log = LogFactory.getLog(PerformFeasibilityTasklet.class);

  private final static FeasibilityStudyQueryBuilder studyQueryBuilder = new FeasibilityStudyQueryBuilder();

  private final JdbcTemplate jdbcTemplate;
  private final TransactionTemplate transactionTemplate;
  private final FeasibilityStudyRepository feasibilityStudyRepository;
  private final CohortDefinitionRepository cohortDefinitionRepository;

  public PerformFeasibilityTasklet(
          final JdbcTemplate jdbcTemplate, 
          final TransactionTemplate transactionTemplate,
          final FeasibilityStudyRepository feasibilityStudyRepository,
          final CohortDefinitionRepository cohortDefinitionRepository) {
    this.jdbcTemplate = jdbcTemplate;
    this.transactionTemplate = transactionTemplate;
    this.feasibilityStudyRepository = feasibilityStudyRepository;
    this.cohortDefinitionRepository = cohortDefinitionRepository;
  }

  private int[] doTask(ChunkContext chunkContext) {
    Map<String, Object> jobParams = chunkContext.getStepContext().getJobParameters();
    Integer studyId = Integer.valueOf(jobParams.get("study_id").toString());
    int[] result = null;
    try {
      FeasibilityStudy p = this.feasibilityStudyRepository.findOne(studyId);
      FeasibilityStudyQueryBuilder.BuildExpressionQueryOptions options = new FeasibilityStudyQueryBuilder.BuildExpressionQueryOptions();
      options.cdmSchema = jobParams.get("cdm_database_schema").toString();
      options.cohortTable = jobParams.get("target_database_schema").toString() + "." + jobParams.get("target_table").toString();
      
      String expressionSql = studyQueryBuilder.buildSimulateQuery(p, options);
      String translatedSql = SqlTranslate.translateSql(expressionSql, "sql server", jobParams.get("target_dialect").toString(), SessionUtils.sessionId(), null);
      String[] sqlStatements = SqlSplit.splitSql(translatedSql);
      result = PerformFeasibilityTasklet.this.jdbcTemplate.batchUpdate(sqlStatements);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return result;
  }

  @Override
  public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
    Integer studyId = Integer.valueOf(chunkContext.getStepContext().getJobParameters().get("study_id").toString());
    Date startTime = Calendar.getInstance().getTime();
    
    DefaultTransactionDefinition requresNewTx = new DefaultTransactionDefinition();
    requresNewTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    
    TransactionStatus initStatus = this.transactionTemplate.getTransactionManager().getTransaction(requresNewTx);
    FeasibilityStudy p = this.feasibilityStudyRepository.findOne(studyId);
    
    CohortDefinition resultDef = p.getResultRule();
    CohortGenerationInfo resultInfo = null; //resultDef.getGenerationInfo();
    resultInfo.setIsValid(false)
            .setStatus(GenerationStatus.RUNNING)
            .setStartTime(startTime)
            .setExecutionDuration(null);
    StudyInfo info = p.getInfo();
    if (info == null)
    {
      info = new StudyInfo(p);
      p.setInfo(info);
    }
    info.setIsValid(false);
    info.setStartTime(startTime);
    info.setStatus(GenerationStatus.RUNNING);
    
    this.feasibilityStudyRepository.save(p);
    this.transactionTemplate.getTransactionManager().commit(initStatus);
    
    try {
      final int[] ret = this.transactionTemplate.execute(new TransactionCallback<int[]>() {

        @Override
        public int[] doInTransaction(final TransactionStatus status) {
          return doTask(chunkContext);
        }
      });
      log.debug("Update count: " + ret.length);
      info.setIsValid(true);
      resultInfo.setIsValid(true);
    } catch (final TransactionException e) {
      info.setIsValid(false);
      resultInfo.setIsValid(false);
      log.error(e.getMessage(), e);
      throw e;//FAIL job status
    }
    finally {
      TransactionStatus completeStatus = this.transactionTemplate.getTransactionManager().getTransaction(requresNewTx);
      Date endTime = Calendar.getInstance().getTime();
      info.setExecutionDuration(new Integer((int)(endTime.getTime() - startTime.getTime())));
      info.setStatus(GenerationStatus.COMPLETE);
      resultInfo.setExecutionDuration(info.getExecutionDuration());
      resultInfo.setStatus(GenerationStatus.COMPLETE);
      this.feasibilityStudyRepository.save(p);
      this.transactionTemplate.getTransactionManager().commit(completeStatus);
    }

    return RepeatStatus.FINISHED;
  }

}