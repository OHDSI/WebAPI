/*
 * Copyright 2015 Observational Health Data Sciences and Informatics <OHDSI.org>.
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
package org.ohdsi.webapi.cohortdefinition;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Calendar;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.sql.SqlTranslate;
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
public class GenerateCohortTasklet implements Tasklet {

  private static final Log log = LogFactory.getLog(GenerateCohortTasklet.class);

  private final static CohortExpressionQueryBuilder expressionQueryBuilder = new CohortExpressionQueryBuilder();

  private final GenerateCohortTask task;
  private final JdbcTemplate jdbcTemplate;
  private final TransactionTemplate transactionTemplate;
  private final CohortDefinitionRepository cohortDefinitionRepository;

  public GenerateCohortTasklet(GenerateCohortTask task, 
          final JdbcTemplate jdbcTemplate, 
          final TransactionTemplate transactionTemplate,
          final CohortDefinitionRepository cohortDefinitionRepository) {
    this.task = task;
    this.jdbcTemplate = jdbcTemplate;
    this.transactionTemplate = transactionTemplate;
    this.cohortDefinitionRepository = cohortDefinitionRepository;
  }

  private int[] doTask() {
    int[] result = null;
    try {
      ObjectMapper mapper = new ObjectMapper();

      CohortExpression expression = mapper.readValue(this.task.getCohortDefinition().getDetails().getExpression(), CohortExpression.class);
      String expressionSql = expressionQueryBuilder.buildExpressionQuery(expression, this.task.getOptions());
      String translatedSql = SqlTranslate.translateSql(expressionSql, this.task.getSourceDialect(), this.task.getTargetDialect());
      String[] sqlStatements = SqlSplit.splitSql(translatedSql);
      result = GenerateCohortTasklet.this.jdbcTemplate.batchUpdate(sqlStatements);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return result;
  }

  @Override
  public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
    Date startTime = Calendar.getInstance().getTime();
    
    DefaultTransactionDefinition initTx = new DefaultTransactionDefinition();
    initTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    TransactionStatus initStatus = this.transactionTemplate.getTransactionManager().getTransaction(initTx);
    CohortDefinition df = this.cohortDefinitionRepository.findOne(this.task.getCohortDefinition().getId());
    CohortGenerationInfo info = df.getGenerationInfo();
    if (info == null)
    {
      info = new CohortGenerationInfo().setCohortDefinition(df);
      df.setGenerationInfo(info);
    }

    info.setIsValid(false);
    info.setStartTime(startTime);
    info.setStatus(GenerationStatus.RUNNING);    
    df = this.cohortDefinitionRepository.save(df);
    this.transactionTemplate.getTransactionManager().commit(initStatus);
    
    info = df.getGenerationInfo();
    
    try {
      final int[] ret = this.transactionTemplate.execute(new TransactionCallback<int[]>() {

        @Override
        public int[] doInTransaction(final TransactionStatus status) {
          return doTask();
        }
      });
      log.debug("Update count: " + ret.length);
      info.setIsValid(true);
    } catch (final Exception e) {
      info.setIsValid(false);
      log.error(e.getMessage(), e);
      throw e;//FAIL job status
    }
    finally {
      DefaultTransactionDefinition completeTx = new DefaultTransactionDefinition();
      completeTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
      TransactionStatus completeStatus = this.transactionTemplate.getTransactionManager().getTransaction(completeTx);      
      Date endTime = Calendar.getInstance().getTime();
      info.setExecutionDuration(new Integer((int)(endTime.getTime() - startTime.getTime())));
      info.setStatus(GenerationStatus.COMPLETE);
      this.cohortDefinitionRepository.save(df);
      this.transactionTemplate.getTransactionManager().commit(completeStatus);
    }

    return RepeatStatus.FINISHED;
  }

}
