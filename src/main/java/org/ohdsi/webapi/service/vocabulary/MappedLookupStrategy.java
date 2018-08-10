package org.ohdsi.webapi.service.vocabulary;

import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.PreparedStatementRenderer;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MappedLookupStrategy implements StatementPrepareStrategy {

  private long[] identifiers;

  public MappedLookupStrategy(long[] identifiers) {

    this.identifiers = identifiers;
  }

  @Override
  public PreparedStatementRenderer prepareStatement(Source source, Function<String, String> queryModifier) {

    String identifiersValue = Arrays.stream(identifiers).boxed()
            .map(String::valueOf).collect(Collectors.joining(","));
    String[] tqName = { "CDM_schema", "identifiers" };
    String[] tqValue = new String[]{ source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary), identifiersValue };
    String resourcePath = "/resources/vocabulary/sql/getMappedSourcecodes.sql";
    String query = ResourceHelper.GetResourceAsString(resourcePath);
    if (Objects.nonNull(queryModifier)) {
      query = queryModifier.apply(query);
    }
    return new PreparedStatementRenderer(source, query, tqName, tqValue, new String[0], new Object[0]);
  }
}
