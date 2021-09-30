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
package org.ohdsi.webapi.ircalc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.odysseusinc.arachne.commons.types.DBMSType;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.common.generation.CancelableTasklet;
import org.ohdsi.webapi.source.SourceService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.util.CancelableJdbcTemplate;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.ohdsi.webapi.util.SourceUtils;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;

import static org.ohdsi.webapi.Constants.Params.*;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
public class IRAnalysisTasklet extends CancelableTasklet {
  
  private final IRAnalysisQueryBuilder analysisQueryBuilder;

  private final IncidenceRateAnalysisRepository incidenceRateAnalysisRepository;
  private final SourceService sourceService;
  private final ObjectMapper objectMapper;

  public IRAnalysisTasklet(
          final CancelableJdbcTemplate jdbcTemplate,
          final TransactionTemplate transactionTemplate,
          final IncidenceRateAnalysisRepository incidenceRateAnalysisRepository,
          final SourceService sourceService,
          final IRAnalysisQueryBuilder analysisQueryBuilder,
          final ObjectMapper objectMapper) {

    super(LoggerFactory.getLogger(IRAnalysisTasklet.class), jdbcTemplate, transactionTemplate);
    this.incidenceRateAnalysisRepository = incidenceRateAnalysisRepository;
    this.sourceService = sourceService;
    this.analysisQueryBuilder = analysisQueryBuilder;
    this.objectMapper = objectMapper;
  }
  
  protected String[] prepareQueries(ChunkContext chunkContext, CancelableJdbcTemplate jdbcTemplate) {
    
    Map<String, Object> jobParams = chunkContext.getStepContext().getJobParameters();

    Integer sourceId = Integer.parseInt(jobParams.get(SOURCE_ID).toString());
    Source source = sourceService.findBySourceId(sourceId);
    String oracleTempSchema = SourceUtils.getTempQualifier(source);

    Integer analysisId = Integer.valueOf(jobParams.get(ANALYSIS_ID).toString());
    String sessionId = jobParams.get(SESSION_ID).toString();
    try {
      IncidenceRateAnalysis analysis = this.incidenceRateAnalysisRepository.findOne(analysisId);
      IncidenceRateAnalysisExpression expression = objectMapper.readValue(analysis.getDetails().getExpression(), IncidenceRateAnalysisExpression.class);
      
      IRAnalysisQueryBuilder.BuildExpressionQueryOptions options = new IRAnalysisQueryBuilder.BuildExpressionQueryOptions();
      options.cdmSchema = SourceUtils.getCdmQualifier(source);
      options.resultsSchema = SourceUtils.getResultsQualifier(source);
      options.vocabularySchema = SourceUtils.getVocabularyQualifier(source);
      options.tempSchema = SourceUtils.getTempQualifier(source);
      options.cohortTable = jobParams.get(TARGET_TABLE).toString();

      String delete = "DELETE FROM @tableQualifier.ir_strata WHERE analysis_id = @analysis_id;";
      PreparedStatementRenderer psr = new PreparedStatementRenderer(source, delete, "tableQualifier",
        options.resultsSchema, "analysis_id", analysisId);
      jdbcTemplate.update(psr.getSql(), psr.getSetter());

      String insert = "INSERT INTO @results_schema.ir_strata (analysis_id, strata_sequence, name, description) VALUES (@analysis_id,@strata_sequence,@name,@description)";

      String [] params = {"analysis_id", "strata_sequence", "name", "description"};
      List<StratifyRule> strataRules = expression.strata;
      for (int i = 0; i< strataRules.size(); i++)
      {
        StratifyRule r = strataRules.get(i);
        psr = new PreparedStatementRenderer(source, insert, "results_schema",
          options.resultsSchema, params, new Object[] { analysisId, i, r.name, r.description});
        jdbcTemplate.update(psr.getSql(), psr.getSetter());
      }
      
      String expressionSql = analysisQueryBuilder.buildAnalysisQuery(analysis, options);
      

      String translatedSql = SqlTranslate.translateSql(expressionSql, source.getSourceDialect(), sessionId, oracleTempSchema);
      return SqlSplit.splitSql(translatedSql);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}