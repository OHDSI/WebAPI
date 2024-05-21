package org.ohdsi.webapi.service.cscompare;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;

public class CompareArbitraryDto {
    public ExpressionType[] types;
    public ConceptSetExpression[] compareTargets;
}
