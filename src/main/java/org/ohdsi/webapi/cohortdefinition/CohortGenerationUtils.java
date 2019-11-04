package org.ohdsi.webapi.cohortdefinition;

import com.google.common.base.MoreObjects;
import org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder;
import org.ohdsi.circe.cohortdefinition.InclusionRule;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.util.SourceUtils;

import java.util.List;

import static org.ohdsi.webapi.Constants.Params.COHORT_ID_FIELD_NAME;
import static org.ohdsi.webapi.Constants.Params.TARGET_COHORT_ID;
import static org.ohdsi.webapi.Constants.Params.TARGET_DATABASE_SCHEMA;

public class CohortGenerationUtils {

  public static String[] buildGenerationSql(CohortGenerationRequest request) {

    Source source = request.getSource();

    String cdmSchema = SourceUtils.getCdmQualifier(source);
    String vocabSchema = SourceUtils.getVocabQualifierOrNull(source);
    String resultsSchema = SourceUtils.getResultsQualifier(source);

    CohortExpressionQueryBuilder expressionQueryBuilder = new CohortExpressionQueryBuilder();
    StringBuilder sqlBuilder = new StringBuilder();

    CohortExpressionQueryBuilder.BuildExpressionQueryOptions options = new CohortExpressionQueryBuilder.BuildExpressionQueryOptions();
    options.cohortIdFieldName = request.getTargetIdFieldName();
    options.cohortId = request.getTargetId();
    options.cdmSchema = cdmSchema;
    options.resultSchema = resultsSchema;
    options.targetTable = request.getTargetSchema() + "." + request.getTargetTable();
    options.vocabularySchema = vocabSchema;
    options.generateStats = request.isGenerateStats();

    final String oracleTempSchema = SourceUtils.getTempQualifier(source);

    if (request.isGenerateStats()) {

      String deleteSql = "DELETE FROM @target_database_schema.cohort_inclusion WHERE @cohort_id_field_name = @target_cohort_id;";
      sqlBuilder.append(deleteSql).append("\n");

      String insertSql = "INSERT INTO @target_database_schema.cohort_inclusion (@cohort_id_field_name, rule_sequence, name, description) SELECT @target_cohort_id as @cohort_id_field_name, @iteration as rule_sequence, CAST('@ruleName' as VARCHAR(255)) as name, CAST('@ruleDescription' as VARCHAR(1000)) as description;";

      String[] names = new String[]{"iteration", "ruleName", "ruleDescription"};
      List<InclusionRule> inclusionRules = request.getExpression().inclusionRules;
      for (int i = 0; i < inclusionRules.size(); i++) {
        InclusionRule r = inclusionRules.get(i);
        String[] values = new String[]{((Integer) i).toString(), r.name, MoreObjects.firstNonNull(r.description, "")};

        String inclusionRuleSql = SqlRender.renderSql(insertSql, names, values);
        sqlBuilder.append(inclusionRuleSql).append("\n");
      }
    }

    String expressionSql = expressionQueryBuilder.buildExpressionQuery(request.getExpression(), options);
    sqlBuilder.append(expressionSql);

    String renderedSql = SqlRender.renderSql(
      sqlBuilder.toString(),
      new String[] {TARGET_DATABASE_SCHEMA, COHORT_ID_FIELD_NAME, TARGET_COHORT_ID},
      new String[]{request.getTargetSchema(), request.getTargetIdFieldName(), request.getTargetId().toString()}
    );
    String translatedSql = SqlTranslate.translateSql(renderedSql, source.getSourceDialect(), request.getSessionId(), oracleTempSchema);
    return SqlSplit.splitSql(translatedSql);
  }
}
