package org.ohdsi.webapi.service.csv;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;

public class CompareArbitraryDto {
    public ConceptSetExpression[] compareTargets;
    public ExpressionType[] types;
}
