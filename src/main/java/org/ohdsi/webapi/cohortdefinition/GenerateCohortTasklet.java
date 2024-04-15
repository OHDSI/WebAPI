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

import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.cohortcharacterization.CCQueryBuilder;
import org.ohdsi.sql.BigQuerySparkTranslate;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.cohortcharacterization.domain.CcFeAnalysisEntity;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.common.generation.CancelableTasklet;
import org.ohdsi.webapi.common.generation.GenerationUtils;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.ohdsi.webapi.feanalysis.repository.FeAnalysisEntityRepository;
import org.ohdsi.webapi.generationcache.GenerationCacheHelper;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceService;
import org.ohdsi.webapi.util.CancelableJdbcTemplate;
import org.ohdsi.webapi.util.SessionUtils;
import org.ohdsi.webapi.util.SourceUtils;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.collect.ImmutableList;
import com.odysseusinc.arachne.commons.types.DBMSType;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.ohdsi.webapi.Constants.Params.*;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
public class GenerateCohortTasklet extends CancelableTasklet implements StoppableTasklet {
  private final static String copyGenerationIntoCohortTableSql = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/copyGenerationIntoCohortTableSql.sql");

  private final GenerationCacheHelper generationCacheHelper;
  private final CohortDefinitionRepository cohortDefinitionRepository;
  private final SourceService sourceService;
  private final FeAnalysisEntityRepository feAnalysisRepository;

  public GenerateCohortTasklet(final CancelableJdbcTemplate jdbcTemplate, final TransactionTemplate transactionTemplate,
          final GenerationCacheHelper generationCacheHelper,
          final CohortDefinitionRepository cohortDefinitionRepository, final SourceService sourceService) {
      super(LoggerFactory.getLogger(GenerateCohortTasklet.class), jdbcTemplate, transactionTemplate);
      this.generationCacheHelper = generationCacheHelper;
      this.cohortDefinitionRepository = cohortDefinitionRepository;
      this.sourceService = sourceService;
      this.feAnalysisRepository = null;
  }

  public GenerateCohortTasklet(
          final CancelableJdbcTemplate jdbcTemplate,
          final TransactionTemplate transactionTemplate,
          final GenerationCacheHelper generationCacheHelper,
          final CohortDefinitionRepository cohortDefinitionRepository,
          final SourceService sourceService, final FeAnalysisEntityRepository feAnalysisRepository
  ) {
    super(LoggerFactory.getLogger(GenerateCohortTasklet.class), jdbcTemplate, transactionTemplate);
    this.generationCacheHelper = generationCacheHelper;
    this.cohortDefinitionRepository = cohortDefinitionRepository;
    this.sourceService = sourceService;
    this.feAnalysisRepository = feAnalysisRepository;
  }

  @Override
  protected String[] prepareQueries(ChunkContext chunkContext, CancelableJdbcTemplate jdbcTemplate) {
      Map<String, Object> jobParams = chunkContext.getStepContext().getJobParameters();

      Boolean demographicStat = jobParams.get(DEMOGRAPHIC_STATS) == null ? null
              : Boolean.valueOf((String) jobParams.get(DEMOGRAPHIC_STATS));

      if (demographicStat != null && demographicStat.booleanValue()) {
          return prepareQueriesDemographic(chunkContext, jdbcTemplate);
      }

      return prepareQueriesDefault(jobParams, jdbcTemplate);
  }

  private String[] prepareQueriesDemographic(ChunkContext chunkContext, CancelableJdbcTemplate jdbcTemplate) {
      Map<String, Object> jobParams = chunkContext.getStepContext().getJobParameters();

      CohortCharacterizationEntity cohortCharacterization = new CohortCharacterizationEntity();

      Integer cohortDefinitionId = Integer.valueOf(jobParams.get(COHORT_DEFINITION_ID).toString());
      CohortDefinition cohortDefinition = cohortDefinitionRepository.findOneWithDetail(cohortDefinitionId);

      cohortCharacterization.setCohortDefinitions(new HashSet<>(Arrays.asList(cohortDefinition)));

      // Get FE Analysis Demographic (Gender, Age, Race,)
      Set<FeAnalysisEntity> feAnalysis = feAnalysisRepository.findByListIds(Arrays.asList(70, 72, 74, 77));

      Set<CcFeAnalysisEntity> ccFeAnalysis = feAnalysis.stream().map(a -> {
          CcFeAnalysisEntity ccA = new CcFeAnalysisEntity();
          ccA.setCohortCharacterization(cohortCharacterization);
          ccA.setFeatureAnalysis(a);
          return ccA;
      }).collect(Collectors.toSet());

      cohortCharacterization.setFeatureAnalyses(ccFeAnalysis);

      final Long jobId = chunkContext.getStepContext().getStepExecution().getJobExecution().getId();

      final Integer sourceId = Integer.valueOf(jobParams.get(SOURCE_ID).toString());
      final Source source = sourceService.findBySourceId(sourceId);

      final String cohortTable = jobParams.get(TARGET_TABLE).toString();
      final String sessionId = jobParams.get(SESSION_ID).toString();

      final String tempSchema = SourceUtils.getTempQualifier(source);

      boolean includeAnnual = false;
      boolean includeTemporal = false;

      CCQueryBuilder ccQueryBuilder = new CCQueryBuilder(cohortCharacterization, cohortTable, sessionId,
              SourceUtils.getCdmQualifier(source), SourceUtils.getResultsQualifier(source),
              SourceUtils.getVocabularyQualifier(source), tempSchema, jobId, includeAnnual, includeTemporal);
      String sql = ccQueryBuilder.build();

      /*
       * There is an issue with temp tables on sql server: Temp tables scope is
       * session or stored procedure. To execute PreparedStatement sql server
       * uses stored procedure <i>sp_executesql</i> and this is the reason why
       * multiple PreparedStatements cannot share the same local temporary
       * table.
       *
       * On the other side, temp tables cannot be re-used in the same
       * PreparedStatement, e.g. temp table cannot be created, used, dropped and
       * created again in the same PreparedStatement because sql optimizator
       * detects object already exists and fails. When is required to re-use
       * temp table it should be separated to several PreparedStatements.
       *
       * An option to use global temp tables also doesn't work since such tables
       * can be not supported / disabled.
       *
       * Therefore, there are two ways: - either precisely group SQLs into
       * statements so that temp tables aren't re-used in a single statement, -
       * or use ‘permanent temporary tables’
       *
       * The second option looks better since such SQL could be exported and
       * executed manually, which is not the case with the first option.
       */
      if (ImmutableList.of(DBMSType.MS_SQL_SERVER.getOhdsiDB(), DBMSType.PDW.getOhdsiDB())
              .contains(source.getSourceDialect())) {
          sql = sql.replaceAll("#", tempSchema + "." + sessionId + "_").replaceAll("tempdb\\.\\.", "");
      }
      if (source.getSourceDialect().equals("spark")) {
          try {
              sql = BigQuerySparkTranslate.sparkHandleInsert(sql, source.getSourceConnection());
          } catch (SQLException e) {
              e.printStackTrace();
          }
      }

      final String translatedSql = SqlTranslate.translateSql(sql, source.getSourceDialect(), sessionId, tempSchema);
      return SqlSplit.splitSql(translatedSql);
  }

  private String[] prepareQueriesDefault(Map<String, Object> jobParams, CancelableJdbcTemplate jdbcTemplate) {
      Integer cohortDefinitionId = Integer.valueOf(jobParams.get(COHORT_DEFINITION_ID).toString());
      Integer sourceId = Integer.parseInt(jobParams.get(SOURCE_ID).toString());
      String targetSchema = jobParams.get(TARGET_DATABASE_SCHEMA).toString();
      String sessionId = jobParams.getOrDefault(SESSION_ID, SessionUtils.sessionId()).toString();

      CohortDefinition cohortDefinition = cohortDefinitionRepository.findOneWithDetail(cohortDefinitionId);
      Source source = sourceService.findBySourceId(sourceId);

      CohortGenerationRequestBuilder generationRequestBuilder = new CohortGenerationRequestBuilder(sessionId,
              targetSchema);

      int designHash = this.generationCacheHelper.computeHash(cohortDefinition.getDetails().getExpression());
      CohortGenerationUtils.insertInclusionRules(cohortDefinition, source, designHash, targetSchema, sessionId,
              jdbcTemplate);

      GenerationCacheHelper.CacheResult res = generationCacheHelper.computeCacheIfAbsent(cohortDefinition, source,
              generationRequestBuilder,
              (resId, sqls) -> generationCacheHelper.runCancelableCohortGeneration(jdbcTemplate, stmtCancel, sqls));

      String sql = SqlRender.renderSql(copyGenerationIntoCohortTableSql,
              new String[] { RESULTS_DATABASE_SCHEMA, COHORT_DEFINITION_ID, DESIGN_HASH },
              new String[] { targetSchema, cohortDefinition.getId().toString(), res.getIdentifier().toString() });
      sql = SqlTranslate.translateSql(sql, source.getSourceDialect());
      return SqlSplit.splitSql(sql);
  }
}
