package org.ohdsi.webapi.cohortdefinition;

import org.apache.commons.lang3.StringUtils;

import org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder;
import org.ohdsi.circe.cohortdefinition.InclusionRule;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.util.SourceUtils;
import org.springframework.jdbc.core.JdbcTemplate;


import java.util.Arrays;
import java.util.List;

import static org.ohdsi.webapi.Constants.Params.TARGET_DATABASE_SCHEMA;
import static org.ohdsi.webapi.Constants.Params.DESIGN_HASH;

import static org.ohdsi.webapi.Constants.Tables.COHORT_CACHE;
import static org.ohdsi.webapi.Constants.Tables.COHORT_CENSOR_STATS_CACHE;
import static org.ohdsi.webapi.Constants.Tables.COHORT_INCLUSION_RESULT_CACHE;
import static org.ohdsi.webapi.Constants.Tables.COHORT_INCLUSION_STATS_CACHE;
import static org.ohdsi.webapi.Constants.Tables.COHORT_SUMMARY_STATS_CACHE;

public class CohortGenerationUtils {

  public static void insertInclusionRules(CohortDefinition cohortDef, Source source, int designHash,
                                          String targetSchema, String sessionId, JdbcTemplate jdbcTemplate) {
    final String oracleTempSchema = SourceUtils.getTempQualifier(source);
    String deleteSql = String.format("DELETE FROM %s.cohort_inclusion WHERE cohort_definition_id = %d;", targetSchema, cohortDef.getId());
    String translatedDeleteSql = SqlTranslate.translateSql(deleteSql, source.getSourceDialect(), sessionId, oracleTempSchema);
    Arrays.stream(SqlSplit.splitSql(translatedDeleteSql)).forEach(jdbcTemplate::execute);

    String insertSql = StringUtils.replace("INSERT INTO @target_schema.cohort_inclusion (cohort_definition_id, design_hash, rule_sequence, name, description) VALUES (?,?,?,?,?)", "@target_schema", targetSchema);
    String translatedInsertSql = SqlTranslate.translateSql(insertSql, source.getSourceDialect(), sessionId, oracleTempSchema);
    List<InclusionRule> inclusionRules = cohortDef.getExpression().inclusionRules;
    for (int i = 0; i< inclusionRules.size(); i++)
    {
      InclusionRule r = inclusionRules.get(i);
      jdbcTemplate.update(translatedInsertSql, new Object[] { cohortDef.getId(), designHash, i, r.name, r.description});
    }
  }
  
  public static String[] buildGenerationSql(CohortGenerationRequest request) {

    Source source = request.getSource();

    String cdmSchema = SourceUtils.getCdmQualifier(source);
    String vocabSchema = SourceUtils.getVocabQualifierOrNull(source);

    CohortExpressionQueryBuilder expressionQueryBuilder = new CohortExpressionQueryBuilder();
    StringBuilder sqlBuilder = new StringBuilder();

    CohortExpressionQueryBuilder.BuildExpressionQueryOptions options = new CohortExpressionQueryBuilder.BuildExpressionQueryOptions();
    options.cohortIdFieldName = DESIGN_HASH;
    options.cohortId = request.getTargetId();
    options.cdmSchema = cdmSchema;
    options.vocabularySchema = vocabSchema;
    options.generateStats = true; // always generate with stats

    final String oracleTempSchema = SourceUtils.getTempQualifier(source);

    String expressionSql = expressionQueryBuilder.buildExpressionQuery(request.getExpression(), options);
    expressionSql = SqlRender.renderSql(
      expressionSql,
      new String[] {"target_cohort_table", 
        "results_database_schema.cohort_inclusion_result", 
        "results_database_schema.cohort_inclusion_stats", 
        "results_database_schema.cohort_summary_stats", 
        "results_database_schema.cohort_censor_stats",
        "results_database_schema.cohort_inclusion" 
      },
      new String[] {
        COHORT_CACHE, 
        "@target_database_schema." + COHORT_INCLUSION_RESULT_CACHE, 
        "@target_database_schema." + COHORT_INCLUSION_STATS_CACHE, 
        "@target_database_schema." + COHORT_SUMMARY_STATS_CACHE, 
        "@target_database_schema." + COHORT_CENSOR_STATS_CACHE,
        "@target_database_schema.cohort_inclusion"
      }
    );
    sqlBuilder.append(expressionSql);

    String renderedSql = SqlRender.renderSql(
      sqlBuilder.toString(),
      new String[] {TARGET_DATABASE_SCHEMA},
      new String[]{request.getTargetSchema()}
    );
    String translatedSql = SqlTranslate.translateSql(renderedSql, source.getSourceDialect(), request.getSessionId(), oracleTempSchema);
    return SqlSplit.splitSql(translatedSql);
  }
}
