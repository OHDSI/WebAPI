package org.ohdsi.webapi.paging;

import org.ohdsi.circe.vocabulary.ConceptSetExpression;

import java.util.List;

public class FacetsRequest {
  private ConceptSetExpression expression;
  private List<FacetColumn> columns;

  public ConceptSetExpression getExpression() {
    return expression;
  }

  public void setExpression(ConceptSetExpression expression) {
    this.expression = expression;
  }

  public List<FacetColumn> getColumns() {
    return columns;
  }

  public void setColumns(List<FacetColumn> columns) {
    this.columns = columns;
  }

  public static class FacetColumn {
    private String columnName;
    private boolean computed;

    public String getColumnName() {
      return columnName;
    }

    public void setColumnName(String columnName) {
      this.columnName = columnName;
    }

    public boolean isComputed() {
      return computed;
    }

    public void setComputed(boolean computed) {
      this.computed = computed;
    }
  }
}
