package org.ohdsi.webapi.service.vocabulary;

import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.util.PreparedStatementRenderer;

import java.util.function.Function;

public interface StatementPrepareStrategy {

  PreparedStatementRenderer prepareStatement(Source source, Function<String, String> queryModifier);
}