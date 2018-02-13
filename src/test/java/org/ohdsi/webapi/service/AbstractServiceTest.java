package org.ohdsi.webapi.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import org.junit.Before;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.ohdsi.webapi.util.SecurityUtils;
import org.springframework.util.StringUtils;

public abstract class AbstractServiceTest {

  private Source source;

  @Before
  public void setupMockedSource() {

    source = mock(Source.class);
    when(getSource().getSourceDialect()).thenReturn("sql server");
    when(getSource().getTableQualifier(SourceDaimon.DaimonType.Results)).thenReturn("result_schema");
    when(getSource().getTableQualifier(SourceDaimon.DaimonType.Vocabulary)).thenReturn("vocab_schema");
    when(getSource().getTableQualifier(SourceDaimon.DaimonType.CDM)).thenReturn("cdm_schema");
    when(getSource().getTableQualifier(SourceDaimon.DaimonType.Evidence)).thenReturn("evidence_schema");
  }

  public static void assertSqlEquals(String expectedPath, PreparedStatementRenderer psr) throws IOException {

    String expectedSql = StringUtils.trimAllWhitespace(ResourceHelper.GetResourceAsString(expectedPath)).toLowerCase();
    assertEquals(expectedSql, StringUtils.trimAllWhitespace(psr.getSql()).toLowerCase());
  }

  public Source getSource() {

    return source;
  }

}
