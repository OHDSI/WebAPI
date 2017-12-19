package org.ohdsi.webapi.util;

import org.junit.Assert;
import org.junit.Test;
import org.ohdsi.webapi.source.Source;

public class PreparedStatementRendererTest {

  private Source source = new Source();
  private String resourcePath;
  private String tableQualifierName;
  private String tableQualifierValue;
  private String sourceDialect;
  private String[] sqlVariableNames;
  private Object[] sqlVariableValues;

  @org.junit.Before
  public void before() {

    sourceDialect = "sql server";
    source.setSourceDialect(sourceDialect);
    resourcePath = "/resources/person/sql/getRecords.sql";
    tableQualifierName = "tableQualifier";
    tableQualifierValue = "omop_v5";
    sqlVariableNames = new String[]{"personId"};
    sqlVariableValues = new Object[]{"1230"};
  }

  @Test(expected = IllegalArgumentException.class)
  public void validateArgumentsWithNullVariableValues() {

    this.sqlVariableValues = null;
    new PreparedStatementRenderer(source, resourcePath, tableQualifierName, tableQualifierValue, sqlVariableNames, sqlVariableValues);
  }

  @Test(expected = IllegalArgumentException.class)
  public void validateArgumentsWithNullVariableNames() {

    this.sqlVariableNames = null;
    new PreparedStatementRenderer(source, resourcePath, tableQualifierName, tableQualifierValue, sqlVariableNames, sqlVariableValues);
  }

  @Test()
  public void validateArgumentsWithNullSourceDialect() {

    sourceDialect = null;
    new PreparedStatementRenderer(source, resourcePath, tableQualifierName, tableQualifierValue, sqlVariableNames, sqlVariableValues);
  }

  @Test(expected = IllegalArgumentException.class)
  public void validateArgumentsWithNullTableQualifierValue() {

    tableQualifierValue = null;
    new PreparedStatementRenderer(source, resourcePath, tableQualifierName, tableQualifierValue, sqlVariableNames, sqlVariableValues);
  }

  @Test
  public void validateArguments() {

    PreparedStatementRenderer u = new PreparedStatementRenderer(source, resourcePath, tableQualifierName, tableQualifierValue, sqlVariableNames, sqlVariableValues);
    Assert.assertNotNull(u);
    Assert.assertNotNull(u.getSql());
    Assert.assertNotNull(u.getSetter());
    Assert.assertNotNull(u.getOrderedParamsList());
  }

  @Test(expected = RuntimeException.class)
  public void validateArgumentsWithInvalidResourcePath() {

    resourcePath += "res.sql";
    new PreparedStatementRenderer(source, resourcePath, tableQualifierName, tableQualifierValue, sqlVariableNames, sqlVariableValues);
  }

}
