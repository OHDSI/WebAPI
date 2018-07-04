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

import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder;
import org.ohdsi.circe.cohortdefinition.InclusionRule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceRepository;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.ohdsi.webapi.util.SessionUtils;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.ohdsi.sql.SqlRender;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
public class GenerateCohortTasklet implements StoppableTasklet {

  private static final Log log = LogFactory.getLog(GenerateCohortTasklet.class);

  private final static CohortExpressionQueryBuilder expressionQueryBuilder = new CohortExpressionQueryBuilder();

  private final JdbcTemplate jdbcTemplate;
  private final TransactionTemplate transactionTemplate;
  private final CohortDefinitionRepository cohortDefinitionRepository;
  private final SourceRepository sourceRepository;
  private final ExecutorService taskExecutor;
  private boolean stopped = false;
  private long checkInterval = 1000;

  public GenerateCohortTasklet(
          final JdbcTemplate jdbcTemplate,
          final TransactionTemplate transactionTemplate,
          final CohortDefinitionRepository cohortDefinitionRepository,
          SourceRepository sourceRepository) {
    this.jdbcTemplate = jdbcTemplate;
    this.transactionTemplate = transactionTemplate;
    this.cohortDefinitionRepository = cohortDefinitionRepository;
    this.sourceRepository = sourceRepository;
    taskExecutor = Executors.newSingleThreadExecutor();
  }

  private int[] doTask(ChunkContext chunkContext) {
    int[] result = new int[0];
    
    Map<String, Object> jobParams = chunkContext.getStepContext().getJobParameters();
    Integer defId = Integer.valueOf(jobParams.get(Constants.Params.COHORT_DEFINITION_ID).toString());
    String sessionId = SessionUtils.sessionId();

    try {
      ObjectMapper mapper = new ObjectMapper();

      DefaultTransactionDefinition requresNewTx = new DefaultTransactionDefinition();
      requresNewTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    
      TransactionStatus initStatus = this.transactionTemplate.getTransactionManager().getTransaction(requresNewTx);
      CohortDefinition def = this.cohortDefinitionRepository.findOne(defId);
      CohortExpression expression = mapper.readValue(def.getDetails().getExpression(), CohortExpression.class);
      this.transactionTemplate.getTransactionManager().commit(initStatus);
      
      CohortExpressionQueryBuilder.BuildExpressionQueryOptions options = new CohortExpressionQueryBuilder.BuildExpressionQueryOptions();
      options.cohortId = defId;
      options.cdmSchema = jobParams.get(Constants.Params.CDM_DATABASE_SCHEMA).toString();
      options.targetTable = jobParams.get(Constants.Params.TARGET_DATABASE_SCHEMA).toString() + "." + jobParams.get(Constants.Params.TARGET_TABLE).toString();
      options.resultSchema = jobParams.get(Constants.Params.RESULTS_DATABASE_SCHEMA).toString();
      if (jobParams.get(Constants.Params.VOCABULARY_DATABASE_SCHEMA) != null)
				options.vocabularySchema = jobParams.get(Constants.Params.VOCABULARY_DATABASE_SCHEMA).toString();
      options.generateStats = Boolean.valueOf(jobParams.get(Constants.Params.GENERATE_STATS).toString());

      Integer sourceId = Integer.parseInt(jobParams.get(Constants.Params.SOURCE_ID).toString());
      Source source = sourceRepository.findBySourceId(sourceId);

      String deleteSql = "DELETE FROM @tableQualifier.cohort_inclusion WHERE cohort_definition_id = @cohortDefinitionId;";
      PreparedStatementRenderer psr = new PreparedStatementRenderer(source, deleteSql, "tableQualifier",
        options.resultSchema, "cohortDefinitionId", options.cohortId);
      if (stopped) {
        return result;
      }
      jdbcTemplate.update(psr.getSql(), psr.getSetter());

//      String insertSql = "INSERT INTO @results_schema.cohort_inclusion (cohort_definition_id, rule_sequence, name, description)  VALUES (@cohortId,@iteration,'@ruleName','@ruleDescription');";
      String insertSql = "INSERT INTO @results_schema.cohort_inclusion (cohort_definition_id, rule_sequence, name, description) SELECT @cohortId as cohort_definition_id, @iteration as rule_sequence, CAST('@ruleName' as VARCHAR(255)) as name, CAST('@ruleDescription' as VARCHAR(1000)) as description;";

			String tqName = "results_schema";
      String tqValue = options.resultSchema;
      String[] names = new String[]{"cohortId", "iteration", "ruleName", "ruleDescription"};
      List<InclusionRule> inclusionRules = expression.inclusionRules;
      for (int i = 0; i < inclusionRules.size(); i++) {
        InclusionRule r = inclusionRules.get(i);
        Object[] values = new Object[]{options.cohortId, i, r.name, r.description};
        psr = new PreparedStatementRenderer(source, insertSql, tqName, tqValue, names, values, sessionId);
        if (stopped) {
          return result;
        }
        jdbcTemplate.update(psr.getSql(), psr.getSetter());
      }
      
      String expressionSql = expressionQueryBuilder.buildExpressionQuery(expression, options);
      expressionSql = SqlRender.renderSql(expressionSql, null, null);
      String translatedSql = SqlTranslate.translateSql(expressionSql, jobParams.get("target_dialect").toString(), sessionId, null);
      String[] sqlStatements = SqlSplit.splitSql(translatedSql);
      FutureTask<int[]> batchUpdateTask = new FutureTask<>(() -> GenerateCohortTasklet.this.jdbcTemplate.batchUpdate(sqlStatements));
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
      throw new RuntimeException(e);
    }

    return result;
  }

  @Override
  public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
   
		final int[] ret = this.transactionTemplate.execute(new TransactionCallback<int[]>() {
			@Override
			public int[] doInTransaction(final TransactionStatus status) {
				return doTask(chunkContext);
			}
		});

		if (this.stopped) {
		  contribution.setExitStatus(ExitStatus.STOPPED);
    }

    return RepeatStatus.FINISHED;
  }

  @Override
  public void stop() {
    this.stopped = true;
  }
}
