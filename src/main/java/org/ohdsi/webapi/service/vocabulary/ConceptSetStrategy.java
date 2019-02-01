package org.ohdsi.webapi.service.vocabulary;

import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.circe.vocabulary.ConceptSetExpressionQueryBuilder;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.PreparedStatementRenderer;

import java.util.Objects;
import java.util.function.Function;

public class ConceptSetStrategy implements StatementPrepareStrategy {

  private ConceptSetExpression expression;

  public ConceptSetStrategy(ConceptSetExpression expression) {
    this.expression = expression;
  }

  @Override
  public PreparedStatementRenderer prepareStatement(Source source, Function<String, String> queryModifier) {

    ConceptSetExpressionQueryBuilder builder = new ConceptSetExpressionQueryBuilder();
    String sql = builder.buildExpressionQuery(expression);
    if (Objects.nonNull(queryModifier)) {
      sql = queryModifier.apply(sql);
    }
    String tqName = "vocabulary_database_schema";
    String vocabularyTableQualifierName = "vocabularyTableQualifier";
    String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
    String vocabularyTableQualifierValue = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);

    String[] tableQualifierNames = {tqName, vocabularyTableQualifierName};
    String[] tableQualifierValues = {tqValue, vocabularyTableQualifierValue};
    sql = SqlRender.renderSql(sql, tableQualifierNames, tableQualifierValues);
    return new PreparedStatementRenderer(source, sql, tableQualifierNames, tableQualifierValues, null);
  }
}