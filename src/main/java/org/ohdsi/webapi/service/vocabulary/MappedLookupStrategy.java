package org.ohdsi.webapi.service.vocabulary;

import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.PreparedStatementRenderer;

import java.util.Objects;
import java.util.function.Function;

public class MappedLookupStrategy implements StatementPrepareStrategy {

  private long[] identifiers;

  public MappedLookupStrategy(long[] identifiers) {

    this.identifiers = identifiers;
  }

  @Override
  public PreparedStatementRenderer prepareStatement(Source source, Function<String, String> queryModifier) {

    String tqName = "CDM_schema";
    String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
    String resourcePath = "/resources/vocabulary/sql/getMappedSourcecodes.sql";
    String query = ResourceHelper.GetResourceAsString(resourcePath);
    if (Objects.nonNull(queryModifier)) {
      query = queryModifier.apply(query);
    }
    return new PreparedStatementRenderer(source, query, tqName, tqValue, "identifiers", identifiers);
  }
}
