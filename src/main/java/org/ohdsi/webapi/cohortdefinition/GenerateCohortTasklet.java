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
import com.google.common.base.MoreObjects;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder;
import org.ohdsi.circe.cohortdefinition.InclusionRule;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.common.generation.CancelableTasklet;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceRepository;
import org.ohdsi.webapi.util.CancelableJdbcTemplate;
import org.ohdsi.webapi.util.JobUtils;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.ohdsi.webapi.util.SessionUtils;
import org.ohdsi.webapi.util.SourceUtils;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;

import static org.ohdsi.webapi.Constants.Params.*;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
public class GenerateCohortTasklet extends CancelableTasklet implements StoppableTasklet {

  private final static CohortExpressionQueryBuilder expressionQueryBuilder = new CohortExpressionQueryBuilder();

  private final CohortDefinitionRepository cohortDefinitionRepository;
  private final SourceRepository sourceRepository;
  private final ObjectMapper mapper;

  public GenerateCohortTasklet(
          final CancelableJdbcTemplate jdbcTemplate,
          final TransactionTemplate transactionTemplate,
          final CohortDefinitionRepository cohortDefinitionRepository,
          final SourceRepository sourceRepository,
          final ObjectMapper mapper) {
    super(LoggerFactory.getLogger(GenerateCohortTasklet.class), jdbcTemplate, transactionTemplate);
    this.cohortDefinitionRepository = cohortDefinitionRepository;
    this.sourceRepository = sourceRepository;
    this.mapper = mapper;
  }

  @Override
  protected String[] prepareQueries(ChunkContext chunkContext, CancelableJdbcTemplate jdbcTemplate) {
    String[] result = new String[0];

    Map<String, Object> jobParams = chunkContext.getStepContext().getJobParameters();
    Integer cohortDefinitionId = Integer.valueOf(jobParams.get(COHORT_DEFINITION_ID).toString());
    Integer sourceId = Integer.parseInt(jobParams.get(SOURCE_ID).toString());
    String sessionId = jobParams.getOrDefault(SESSION_ID, SessionUtils.sessionId()).toString();
    String cdmSchema = JobUtils.getSchema(jobParams, CDM_DATABASE_SCHEMA);
    String resultsSchema = JobUtils.getSchema(jobParams, RESULTS_DATABASE_SCHEMA);
    String targetSchema = JobUtils.getSchema(jobParams, TARGET_DATABASE_SCHEMA);
    String targetTable = jobParams.get(TARGET_TABLE).toString();
    Object vocabSchema = jobParams.get(VOCABULARY_DATABASE_SCHEMA);
    boolean generateStats = Boolean.valueOf(jobParams.get(GENERATE_STATS).toString());
    String targetDialect = jobParams.get("target_dialect").toString();

    try {
      DefaultTransactionDefinition requresNewTx = new DefaultTransactionDefinition();
      requresNewTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

      TransactionStatus initStatus = this.transactionTemplate.getTransactionManager().getTransaction(requresNewTx);
      CohortDefinition def = this.cohortDefinitionRepository.findOne(cohortDefinitionId);
      CohortExpression expression = mapper.readValue(def.getDetails().getExpression(), CohortExpression.class);
      this.transactionTemplate.getTransactionManager().commit(initStatus);

      CohortExpressionQueryBuilder.BuildExpressionQueryOptions options = new CohortExpressionQueryBuilder.BuildExpressionQueryOptions();
      options.cohortId = cohortDefinitionId;
      options.cdmSchema = cdmSchema;
      options.resultSchema = resultsSchema;
      options.targetTable = targetSchema + "." + targetTable;
      if (vocabSchema != null){
        options.vocabularySchema = vocabSchema.toString();
      }        
      options.generateStats = generateStats;

      Source source = sourceRepository.findBySourceId(sourceId);
      final String oracleTempSchema = SourceUtils.getTempQualifier(source);

      if (generateStats) {

        String deleteSql = "DELETE FROM @target_database_schema.cohort_inclusion WHERE cohort_definition_id = @cohortDefinitionId;";
        PreparedStatementRenderer psr = new PreparedStatementRenderer(source, deleteSql, "target_database_schema",
                targetSchema, "cohortDefinitionId", options.cohortId);
        if (isStopped()) {
          return result;
        }
        jdbcTemplate.update(psr.getSql(), psr.getSetter());

  //      String insertSql = "INSERT INTO @results_schema.cohort_inclusion (cohort_definition_id, rule_sequence, name, description)  VALUES (@cohortId,@iteration,'@ruleName','@ruleDescription');";
        String insertSql = "INSERT INTO @target_database_schema.cohort_inclusion (cohort_definition_id, rule_sequence, name, description) SELECT @cohortId as cohort_definition_id, @iteration as rule_sequence, CAST('@ruleName' as VARCHAR(255)) as name, CAST('@ruleDescription' as VARCHAR(1000)) as description;";

        String[] names = new String[]{"cohortId", "iteration", "ruleName", "ruleDescription"};
        List<InclusionRule> inclusionRules = expression.inclusionRules;
        for (int i = 0; i < inclusionRules.size(); i++) {
          InclusionRule r = inclusionRules.get(i);
          Object[] values = new Object[]{options.cohortId, i, r.name, MoreObjects.firstNonNull(r.description, "")};
          psr = new PreparedStatementRenderer(source, insertSql, "target_database_schema", targetSchema, names, values, sessionId);
          if (isStopped()) {
            return result;
          }
          jdbcTemplate.update(psr.getSql(), psr.getSetter());
        }
      }

      String expressionSql = expressionQueryBuilder.buildExpressionQuery(expression, options);
      expressionSql = SqlRender.renderSql(expressionSql, null, null);
      String translatedSql = SqlTranslate.translateSql(expressionSql, targetDialect, sessionId, oracleTempSchema);
      return SqlSplit.splitSql(translatedSql);
    } catch (Exception e) {
      log.error("Failed to generate cohort: {}", cohortDefinitionId, e);
      throw new RuntimeException(e);
    }
  }
}
