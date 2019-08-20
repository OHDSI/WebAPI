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
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.common.generation.CancelableTasklet;
import org.ohdsi.webapi.generationcache.GenerationCacheHelper;
import org.ohdsi.webapi.service.CohortGenerationService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceService;
import org.ohdsi.webapi.util.CancelableJdbcTemplate;
import org.ohdsi.webapi.util.SessionUtils;
import org.ohdsi.webapi.util.SourceUtils;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;

import static org.ohdsi.webapi.Constants.Params.*;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
public class GenerateCohortTasklet extends CancelableTasklet implements StoppableTasklet {

  private final CohortGenerationService cohortGenerationService;
  private final GenerationCacheHelper generationCacheHelper;
  private final CohortDefinitionRepository cohortDefinitionRepository;
  private final SourceService sourceService;

  public GenerateCohortTasklet(
          final CancelableJdbcTemplate jdbcTemplate,
          final TransactionTemplate transactionTemplate,
          final CohortGenerationService cohortGenerationService,
          final GenerationCacheHelper generationCacheHelper,
          final CohortDefinitionRepository cohortDefinitionRepository,
          final SourceService sourceService
  ) {
    super(LoggerFactory.getLogger(GenerateCohortTasklet.class), jdbcTemplate, transactionTemplate);
    this.cohortGenerationService = cohortGenerationService;
    this.generationCacheHelper = generationCacheHelper;
    this.cohortDefinitionRepository = cohortDefinitionRepository;
    this.sourceService = sourceService;
  }

  @Override
  protected String[] prepareQueries(ChunkContext chunkContext, CancelableJdbcTemplate jdbcTemplate) {

    Map<String, Object> jobParams = chunkContext.getStepContext().getJobParameters();

    Integer cohortDefinitionId = Integer.valueOf(jobParams.get(COHORT_DEFINITION_ID).toString());
    Integer sourceId = Integer.parseInt(jobParams.get(SOURCE_ID).toString());

    CohortDefinition cohortDefinition = cohortDefinitionRepository.findOneWithDetail(cohortDefinitionId);
    CohortExpression expression = cohortDefinition.getDetails().getExpressionObject();

    Source source = sourceService.findBySourceId(sourceId);

    String resultSql = generationCacheHelper.computeIfAbsent(cohortDefinition, source, resultIdentifier -> {
       String[] sqls = cohortGenerationService.buildGenerationSql(
          expression,
          sourceId,
          jobParams.getOrDefault(SESSION_ID, SessionUtils.sessionId()).toString(),
          jobParams.get(TARGET_DATABASE_SCHEMA).toString(),
          Constants.Tables.COHORT_GENERATIONS_TABLE,
          resultIdentifier,
          Boolean.valueOf(jobParams.get(GENERATE_STATS).toString())
      );
      jdbcTemplate.batchUpdate(stmtCancel, sqls);
    });

    String copyGenerationIntoCohortTableSql = "INSERT INTO @results_database_schema.cohort SELECT @cohort_definition_id, subject_id, cohort_start_date, cohort_end_date FROM (@cached_result_sql) generations;";
    String sql = SqlRender.renderSql(
        copyGenerationIntoCohortTableSql,
        new String[]{ RESULTS_DATABASE_SCHEMA, COHORT_DEFINITION_ID, "cached_result_sql" },
        new String[]{ SourceUtils.getResultsQualifier(source), cohortDefinition.getId().toString(), resultSql }
    );
    sql = SqlTranslate.translateSql(sql, source.getSourceDialect());
    return new String[] {sql};
  }
}
