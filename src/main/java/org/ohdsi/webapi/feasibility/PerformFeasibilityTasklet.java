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

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.CohortGenerationInfo;
import org.ohdsi.webapi.GenerationStatus;
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
  private final static String CREATE_TEMP_TABLES_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/feasibility/sql/inclusionRuleTable_CREATE.sql"); 
  private final static String DROP_TEMP_TABLES_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/feasibility/sql/inclusionRuleTable_DROP.sql"); 

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

  private StudyGenerationInfo findStudyGenerationInfoBySourceId(Collection<StudyGenerationInfo> infoList, Integer sourceId)
  {
    for (StudyGenerationInfo info : infoList) {
      if (info.getId().getSourceId()== sourceId)
        return info;
    }
    return null;
  }
  
  private CohortGenerationInfo findCohortGenerationInfoBySourceId(Collection<CohortGenerationInfo> infoList, Integer sourceId)
  {
    for (CohortGenerationInfo info : infoList) {
      if (info.getId().getSourceId()== sourceId)
        return info;
    }
    return null;
  }
  
  private void prepareTempTables(FeasibilityStudy study, String dialect, String sessionId)
  {
      String translatedSql = SqlTranslate.translateSql(CREATE_TEMP_TABLES_TEMPLATE, "sql server", dialect, sessionId, null);
      String[] sqlStatements = SqlSplit.splitSql(translatedSql);
      this.jdbcTemplate.batchUpdate(sqlStatements);
      
      String insertSql = SqlTranslate.translateSql("INSERT INTO #inclusionRules (study_id, sequence, name) VALUES (?,?,?)","sql server", dialect, sessionId, null);
      List<InclusionRule> inclusionRules = study.getInclusionRules();
      for (int i = 0; i< inclusionRules.size(); i++)
      {
        InclusionRule r = inclusionRules.get(i);
        this.jdbcTemplate.update(insertSql, new Object[] { study.getId(), i, r.getName()});
      }
  }
  
  private void cleanupTempTables(String dialect, String sessionId)
  {
    String translatedSql = SqlTranslate.translateSql(DROP_TEMP_TABLES_TEMPLATE, "sql server", dialect, sessionId, null);
    String[] sqlStatements = SqlSplit.splitSql(translatedSql);
    this.jdbcTemplate.batchUpdate(sqlStatements);
  }
  
  private int[] doTask(ChunkContext chunkContext) {
    Map<String, Object> jobParams = chunkContext.getStepContext().getJobParameters();
    Integer studyId = Integer.valueOf(jobParams.get("study_id").toString());
    int[] result = null;
    try {
      String sessionId = SessionUtils.sessionId();
      FeasibilityStudy study = this.feasibilityStudyRepository.findOne(studyId);
      FeasibilityStudyQueryBuilder.BuildExpressionQueryOptions options = new FeasibilityStudyQueryBuilder.BuildExpressionQueryOptions();
      options.cdmSchema = jobParams.get("cdm_database_schema").toString();
      options.ohdsiSchema = jobParams.get("target_database_schema").toString();
      options.cohortTable = jobParams.get("target_database_schema").toString() + "." + jobParams.get("target_table").toString();
      if (study.getResultRule() != null) {
        prepareTempTables(study, jobParams.get("target_dialect").toString(), sessionId);
        String expressionSql = studyQueryBuilder.buildSimulateQuery(study, options);
        String translatedSql = SqlTranslate.translateSql(expressionSql, "sql server", jobParams.get("target_dialect").toString(), sessionId, null);
        String[] sqlStatements = SqlSplit.splitSql(translatedSql);
        result = PerformFeasibilityTasklet.this.jdbcTemplate.batchUpdate(sqlStatements);
        cleanupTempTables(jobParams.get("target_dialect").toString(), sessionId);
      }
      else {
        String expressionSql = studyQueryBuilder.buildNullQuery(study, options);
        String translatedSql = SqlTranslate.translateSql(expressionSql, "sql server", jobParams.get("target_dialect").toString(), sessionId, null);
        String[] sqlStatements = SqlSplit.splitSql(translatedSql);
        result = PerformFeasibilityTasklet.this.jdbcTemplate.batchUpdate(sqlStatements);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  @Override
  public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
    Date startTime = Calendar.getInstance().getTime();
    Map<String, Object> jobParams = chunkContext.getStepContext().getJobParameters();
    Integer studyId = Integer.valueOf(jobParams.get("study_id").toString());
    Integer sourceId = Integer.valueOf(jobParams.get("source_id").toString());
    boolean isValid = false;

    DefaultTransactionDefinition requresNewTx = new DefaultTransactionDefinition();
    requresNewTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    
    TransactionStatus initStatus = this.transactionTemplate.getTransactionManager().getTransaction(requresNewTx);
    FeasibilityStudy study = this.feasibilityStudyRepository.findOne(studyId);
    
    CohortDefinition resultDef = study.getResultRule();
    if (resultDef != null)
    {
      CohortGenerationInfo resultInfo = findCohortGenerationInfoBySourceId(resultDef.getGenerationInfoList(), sourceId);
      resultInfo.setIsValid(false)
              .setStatus(GenerationStatus.RUNNING)
              .setStartTime(startTime)
              .setExecutionDuration(null);
    }
    StudyGenerationInfo studyInfo = findStudyGenerationInfoBySourceId(study.getStudyGenerationInfoList(), sourceId);
    studyInfo.setIsValid(false);
    studyInfo.setStartTime(startTime);
    studyInfo.setStatus(GenerationStatus.RUNNING);
    
    this.feasibilityStudyRepository.save(study);
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
    } catch (final TransactionException e) {
      isValid = false;
      log.error(e.getMessage(), e);
      throw e;//FAIL job status
    }
    finally {
      TransactionStatus completeStatus = this.transactionTemplate.getTransactionManager().getTransaction(requresNewTx);
      Date endTime = Calendar.getInstance().getTime();
      study = this.feasibilityStudyRepository.findOne(studyId);
      resultDef = study.getResultRule();
      if (resultDef != null)
      {
        CohortGenerationInfo resultInfo = findCohortGenerationInfoBySourceId(resultDef.getGenerationInfoList(), sourceId);
        resultInfo = findCohortGenerationInfoBySourceId(resultDef.getGenerationInfoList(), sourceId);
        resultInfo.setIsValid(isValid);
        resultInfo.setExecutionDuration(new Integer((int)(endTime.getTime() - startTime.getTime())));
        resultInfo.setStatus(GenerationStatus.COMPLETE);
      }
      
      studyInfo = findStudyGenerationInfoBySourceId(study.getStudyGenerationInfoList(), sourceId);
      studyInfo.setIsValid(isValid);
      studyInfo.setExecutionDuration(new Integer((int)(endTime.getTime() - startTime.getTime())));
      studyInfo.setStatus(GenerationStatus.COMPLETE);
      
      this.feasibilityStudyRepository.save(study);
      this.transactionTemplate.getTransactionManager().commit(completeStatus);
    }

    return RepeatStatus.FINISHED;
  }

}