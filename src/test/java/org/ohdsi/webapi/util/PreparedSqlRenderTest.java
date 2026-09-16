package org.ohdsi.webapi.util;

import org.junit.Assert;
import org.junit.Test;
import org.ohdsi.webapi.source.Source;

public class PreparedSqlRenderTest {

  private static Source sourceWithDialect(String dialect) {
    Source source = new Source();
    source.setSourceDialect(dialect);
    return source;
  }

  @Test
  public void parameterLimitPerDialect() {

    Assert.assertEquals(990, PreparedSqlRender.getParameterLimit(sourceWithDialect("oracle")));
    Assert.assertEquals(2000, PreparedSqlRender.getParameterLimit(sourceWithDialect("sql server")));
    Assert.assertEquals(2000, PreparedSqlRender.getParameterLimit(sourceWithDialect("pdw")));
    Assert.assertEquals(10000, PreparedSqlRender.getParameterLimit(sourceWithDialect("bigquery")));
    Assert.assertEquals(10000, PreparedSqlRender.getParameterLimit(sourceWithDialect("snowflake")));
    // Databricks sources use the spark dialect and reject statements with more than 10000 parameters
    Assert.assertEquals(10000, PreparedSqlRender.getParameterLimit(sourceWithDialect("spark")));
    // dialects without a known limit keep the default
    Assert.assertEquals(30000, PreparedSqlRender.getParameterLimit(sourceWithDialect("postgresql")));
  }

  @Test
  public void parameterLimitIsCaseInsensitive() {

    Assert.assertEquals(10000, PreparedSqlRender.getParameterLimit(sourceWithDialect("SPARK")));
    Assert.assertEquals(990, PreparedSqlRender.getParameterLimit(sourceWithDialect("Oracle")));
  }
}
