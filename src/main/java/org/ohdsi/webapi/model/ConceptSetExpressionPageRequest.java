package org.ohdsi.webapi.model;

import org.ohdsi.circe.vocabulary.ConceptSetExpression;

public class ConceptSetExpressionPageRequest extends PageRequest {
  private ConceptSetExpression expression;

  public ConceptSetExpression getExpression() {
    return expression;
  }

  public void setExpression(ConceptSetExpression expression) {
    this.expression = expression;
  }
}
