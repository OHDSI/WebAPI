package org.ohdsi.webapi.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ohdsi.webapi.source.Source;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class PreparedStatementRendererTest {

  private Source source = new Source();
  private String resourcePath;
  private String tableQualifierName;
  private String tableQualifierValue;
  private String sourceDialect;
  private String[] sqlVariableNames;
  private Object[] sqlVariableValues;

  @org.junit.jupiter.api.BeforeEach
  public void before() {

    sourceDialect = "sql server";
    source.setSourceDialect(sourceDialect);
    resourcePath = "/resources/person/sql/getRecords.sql";
    tableQualifierName = "tableQualifier";
    tableQualifierValue = "omop_v5";
    sqlVariableNames = new String[]{"personId"};
    sqlVariableValues = new Object[]{"1230"};
  }

  @Test
  public void validateArgumentsWithNullVariableValues() {
      assertThrows(IllegalArgumentException.class, () -> {

          this.sqlVariableValues = null;
          new PreparedStatementRenderer(source, resourcePath, tableQualifierName, tableQualifierValue, sqlVariableNames, sqlVariableValues);
      });
  }

  @Test
  public void validateArgumentsWithNullVariableNames() {
      assertThrows(IllegalArgumentException.class, () -> {

          this.sqlVariableNames = null;
          new PreparedStatementRenderer(source, resourcePath, tableQualifierName, tableQualifierValue, sqlVariableNames, sqlVariableValues);
      });
  }

  @Test
  public void validateArgumentsWithNullSourceDialect() {

    sourceDialect = null;
    new PreparedStatementRenderer(source, resourcePath, tableQualifierName, tableQualifierValue, sqlVariableNames, sqlVariableValues);
  }

  @Test
  public void validateArgumentsWithNullTableQualifierValue() {
      assertThrows(IllegalArgumentException.class, () -> {

          tableQualifierValue = null;
          new PreparedStatementRenderer(source, resourcePath, tableQualifierName, tableQualifierValue, sqlVariableNames, sqlVariableValues);
      });
  }

  @Test
  public void validateArguments() {

    PreparedStatementRenderer u = new PreparedStatementRenderer(source, resourcePath, tableQualifierName, tableQualifierValue, sqlVariableNames, sqlVariableValues);
    Assertions.assertNotNull(u);
    Assertions.assertNotNull(u.getSql());
    Assertions.assertNotNull(u.getSetter());
    Assertions.assertNotNull(u.getOrderedParamsList());
  }

  @Test
  public void validateArgumentsWithInvalidResourcePath() {
      assertThrows(RuntimeException.class, () -> {

          resourcePath += "res.sql";
          new PreparedStatementRenderer(source, resourcePath, tableQualifierName, tableQualifierValue, sqlVariableNames, sqlVariableValues);
      });
  }

}
