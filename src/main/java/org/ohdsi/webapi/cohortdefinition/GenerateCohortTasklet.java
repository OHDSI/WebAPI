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
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.common.generation.CancelableTasklet;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceRepository;
import org.ohdsi.webapi.util.CancelableJdbcTemplate;
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

  public GenerateCohortTasklet(
          final CancelableJdbcTemplate jdbcTemplate,
          final TransactionTemplate transactionTemplate,
          final CohortDefinitionRepository cohortDefinitionRepository,
          SourceRepository sourceRepository) {
    super(LoggerFactory.getLogger(GenerateCohortTasklet.class), jdbcTemplate, transactionTemplate);
    this.cohortDefinitionRepository = cohortDefinitionRepository;
    this.sourceRepository = sourceRepository;
  }

  @Override
  protected String[] prepareQueries(ChunkContext chunkContext, CancelableJdbcTemplate jdbcTemplate) {
    String[] result = new String[0];

    Map<String, Object> jobParams = chunkContext.getStepContext().getJobParameters();
    Integer defId = Integer.valueOf(jobParams.get(Constants.Params.COHORT_DEFINITION_ID).toString());
    String sessionId = jobParams.getOrDefault(SESSION_ID, SessionUtils.sessionId()).toString();

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
      final String targetSchema = jobParams.get(TARGET_DATABASE_SCHEMA).toString();
      options.cdmSchema = jobParams.get(Constants.Params.CDM_DATABASE_SCHEMA).toString();
      options.targetTable = targetSchema + "." + jobParams.get(Constants.Params.TARGET_TABLE).toString();
      options.resultSchema = jobParams.get(Constants.Params.RESULTS_DATABASE_SCHEMA).toString();
      if (jobParams.get(Constants.Params.VOCABULARY_DATABASE_SCHEMA) != null)
        options.vocabularySchema = jobParams.get(Constants.Params.VOCABULARY_DATABASE_SCHEMA).toString();
      options.generateStats = Boolean.valueOf(jobParams.get(Constants.Params.GENERATE_STATS).toString());

      Integer sourceId = Integer.parseInt(jobParams.get(Constants.Params.SOURCE_ID).toString());
      Source source = sourceRepository.findBySourceId(sourceId);
      final String oracleTempSchema = SourceUtils.getTempQualifier(source);

      if (jobParams.get(GENERATE_STATS).equals(Boolean.TRUE.toString())) {

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
      String translatedSql = SqlTranslate.translateSql(expressionSql, jobParams.get("target_dialect").toString(), sessionId, oracleTempSchema);
      return SqlSplit.splitSql(translatedSql);
    } catch (Exception e) {
      log.error("Failed to generate cohort: {}", defId, e);
      throw new RuntimeException(e);
    }
  }

}
