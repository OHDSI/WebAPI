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

  private final PerformFeasibilityTask task;
  private final JdbcTemplate jdbcTemplate;
  private final TransactionTemplate transactionTemplate;
  private final FeasibilityStudyRepository feasibilityStudyRepository;
  private final CohortDefinitionRepository cohortDefinitionRepository;

  public PerformFeasibilityTasklet(PerformFeasibilityTask task, 
          final JdbcTemplate jdbcTemplate, 
          final TransactionTemplate transactionTemplate,
          final FeasibilityStudyRepository feasibilityStudyRepository,
          final CohortDefinitionRepository cohortDefinitionRepository) {
    this.task = task;
    this.jdbcTemplate = jdbcTemplate;
    this.transactionTemplate = transactionTemplate;
    this.feasibilityStudyRepository = feasibilityStudyRepository;
    this.cohortDefinitionRepository = cohortDefinitionRepository;
  }

  private int[] doTask(ChunkContext chunkContext) {
    Integer studyId = Integer.valueOf(chunkContext.getStepContext().getJobParameters().get("study_id").toString());
    int[] result = null;
    try {
      FeasibilityStudy p = this.feasibilityStudyRepository.findOne(studyId);
      
      String expressionSql = studyQueryBuilder.buildSimulateQuery(p, task.getOptions());
      String translatedSql = SqlTranslate.translateSql(expressionSql, this.task.getSourceDialect(), this.task.getTargetDialect());
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
    
    // get result definition, create a new definition if it does not exist.
    CohortDefinition resultDef = p.getResultRule();
    if (resultDef == null)
    {
      resultDef = new CohortDefinition()
              .setCreatedBy("system")
              .setCreatedDate(startTime)
              .setExpressionType(ExpressionType.SIMPLE_EXPRESSION)
              .setName("Matching Population for Feasability Study: " + p.getName())
              .setDescription("Created by Feasability Study Process");
      resultDef = this.cohortDefinitionRepository.save(resultDef);
      p.setResultRule(resultDef);
    }
    CohortDefinitionDetails resultDetails = resultDef.getDetails();
    if (resultDetails == null)
    {
      resultDetails = new CohortDefinitionDetails();
      resultDetails.setCohortDefinition(resultDef);
      resultDef.setDetails(resultDetails);
    }
    CohortGenerationInfo resultInfo = resultDef.getGenerationInfo();
    if (resultInfo == null)
    {
      resultInfo = new CohortGenerationInfo().setCohortDefinition(resultDef);
      resultDef.setGenerationInfo(resultInfo);
    }
    resultInfo.setIsValid(false)
            .setStatus(GenerationStatus.RUNNING)
            .setStartTime(startTime)
            .setExecutionDuration(null);

    // all resultRule repository objects are initalized; create 'all criteria' cohort definition from index rule + inclusion rules
    ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
    CohortExpression indexRuleExpression = mapper.readValue(p.getIndexRule().getDetails().getExpression(), CohortExpression.class);
    if (p.getInclusionRules().size() > 0) // if we have inclusion rules, add then to the index rule expression, else the matching expression == index rule expression
    {
      if (indexRuleExpression.additionalCriteria == null)
      {
        CriteriaGroup additionalCriteria = new CriteriaGroup();
        additionalCriteria.type="ALL";
        indexRuleExpression.additionalCriteria = additionalCriteria;
      } else {
        if ("ANY".equalsIgnoreCase(indexRuleExpression.additionalCriteria.type))
        {
          // move this CriteriaGroup inside a new parent CriteriaGroup where the parent CriteriaGroup.type == "ALL"
          CriteriaGroup parentGroup = new CriteriaGroup();
          parentGroup.type = "ALL";
          parentGroup.groups = new CriteriaGroup[1];
          parentGroup.groups[0] = indexRuleExpression.additionalCriteria;
          indexRuleExpression.additionalCriteria = parentGroup;
        }
      }
      // place each inclusion rule (which is a CriteriaGroup) in the indexRuleExpression.additionalCriteria.group array to create the 'allCriteriaExpression'
      ArrayList<CriteriaGroup> additionalCriteriaGroups = new ArrayList<>();
      if (indexRuleExpression.additionalCriteria.groups != null)
        additionalCriteriaGroups.addAll(Arrays.asList(indexRuleExpression.additionalCriteria.groups));

      for(InclusionRule inclusionRule : p.getInclusionRules())
      {
        String inclusionRuleJSON = inclusionRule.getExpression();
        CriteriaGroup inclusionRuleGroup =  mapper.readValue(inclusionRuleJSON, CriteriaGroup.class);
        additionalCriteriaGroups.add(inclusionRuleGroup);
      }
      // overwrite indexRule additional criteria groups with the new list of groups with inclusion rules
      indexRuleExpression.additionalCriteria.groups = additionalCriteriaGroups.toArray(new CriteriaGroup[0]);
    }
    String allCriteriaExpression = mapper.writeValueAsString(indexRuleExpression); // index rule expression now contains all inclusion criteria as additional criteria
    resultDetails.setExpression(allCriteriaExpression);
    
    resultDef  = cohortDefinitionRepository.save(resultDef);
    p.setResultRule(resultDef);
    
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