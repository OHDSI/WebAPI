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

import org.ohdsi.webapi.common.generation.CancelableTasklet;
import org.ohdsi.webapi.service.CohortGenerationService;
import org.ohdsi.webapi.util.CancelableJdbcTemplate;
import org.ohdsi.webapi.util.SessionUtils;
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

  public GenerateCohortTasklet(
          final CancelableJdbcTemplate jdbcTemplate,
          final TransactionTemplate transactionTemplate,
          final CohortGenerationService cohortGenerationService
  ) {
    super(LoggerFactory.getLogger(GenerateCohortTasklet.class), jdbcTemplate, transactionTemplate);
    this.cohortGenerationService = cohortGenerationService;
  }

  @Override
  protected String[] prepareQueries(ChunkContext chunkContext, CancelableJdbcTemplate jdbcTemplate) {

    Map<String, Object> jobParams = chunkContext.getStepContext().getJobParameters();
    return cohortGenerationService.buildGenerationSql(
        Integer.valueOf(jobParams.get(COHORT_DEFINITION_ID).toString()),
        Integer.parseInt(jobParams.get(SOURCE_ID).toString()),
        jobParams.getOrDefault(SESSION_ID, SessionUtils.sessionId()).toString(),
        jobParams.get(TARGET_DATABASE_SCHEMA).toString(),
        jobParams.get(TARGET_TABLE).toString(),
        Boolean.valueOf(jobParams.get(GENERATE_STATS).toString())
    );
  }
}
