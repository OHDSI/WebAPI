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

import static org.ohdsi.webapi.util.SecurityUtils.whitelist;

import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder;
import org.ohdsi.circe.cohortdefinition.InclusionRule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.sql.SqlTranslate;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.ohdsi.sql.SqlRender;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
public class GenerateCohortTasklet implements Tasklet {

  private static final Log log = LogFactory.getLog(GenerateCohortTasklet.class);

  private final static CohortExpressionQueryBuilder expressionQueryBuilder = new CohortExpressionQueryBuilder();

  private final JdbcTemplate jdbcTemplate;
  private final TransactionTemplate transactionTemplate;
  private final CohortDefinitionRepository cohortDefinitionRepository;

  public GenerateCohortTasklet(
          final JdbcTemplate jdbcTemplate, 
          final TransactionTemplate transactionTemplate,
          final CohortDefinitionRepository cohortDefinitionRepository) {
    this.jdbcTemplate = jdbcTemplate;
    this.transactionTemplate = transactionTemplate;
    this.cohortDefinitionRepository = cohortDefinitionRepository;
  }

  private int[] doTask(ChunkContext chunkContext) {
    int[] result;
    
    Map<String, Object> jobParams = chunkContext.getStepContext().getJobParameters();
    Integer defId = Integer.valueOf(jobParams.get("cohort_definition_id").toString());
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
      options.cdmSchema = jobParams.get("cdm_database_schema").toString();
      options.targetTable = jobParams.get("target_database_schema").toString() + "." + jobParams.get("target_table").toString();
      options.resultSchema = jobParams.get("results_database_schema").toString();
      if (jobParams.get("vocabulary_database_schema") != null)
				options.vocabularySchema = jobParams.get("vocabulary_database_schema").toString();
      options.generateStats = Boolean.valueOf(jobParams.get("generate_stats").toString());

      String deleteSql = "DELETE FROM @tableQualifier.cohort_inclusion WHERE cohort_definition_id = @cohortDefinitionId";
      PreparedStatementRenderer psr = new PreparedStatementRenderer(null, deleteSql, "tableQualifier",
        options.resultSchema, "cohortDefinitionId", options.cohortId);
      jdbcTemplate.update(psr.getSql(), psr.getSetter());

      String insertSql = "INSERT INTO @results_schema.cohort_inclusion (cohort_definition_id, rule_sequence, name, description) VALUES (@cohortId,@iteration,@ruleName,@ruleDescription)";
      String tqName = "results_schema";
      String tqValue = options.resultSchema;
      String[] names = new String[]{"cohortId", "iteration", "ruleName", "ruleDescription"};
      List<InclusionRule> inclusionRules = expression.inclusionRules;
      for (int i = 0; i < inclusionRules.size(); i++) {
        InclusionRule r = inclusionRules.get(i);
        Object[] values = new Object[]{options.cohortId, i, r.name, r.description};
        psr = new PreparedStatementRenderer(null, insertSql, tqName, tqValue, names, values, sessionId);
        jdbcTemplate.update(psr.getSql(), psr.getSetter());
      }
      
      String expressionSql = expressionQueryBuilder.buildExpressionQuery(expression, options);
      expressionSql = SqlRender.renderSql(expressionSql, null, null);
      String translatedSql = SqlTranslate.translateSql(expressionSql, jobParams.get("target_dialect").toString(), sessionId, null);
      String[] sqlStatements = SqlSplit.splitSql(translatedSql);
      result = GenerateCohortTasklet.this.jdbcTemplate.batchUpdate(sqlStatements);

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

    return RepeatStatus.FINISHED;
  }
}
