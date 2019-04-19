package org.ohdsi.webapi.cohortdefinition.dto;

import org.ohdsi.analysis.Cohort;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.webapi.cohortdefinition.ExpressionType;

public class CohortDTO extends CohortMetadataDTO implements Cohort{

    private String expression;
    private ExpressionType expressionType;
    
    public CohortExpression getExpression() {
        return CohortExpression.fromJson(expression);
    }

    public String getExpressionStr() {
        return expression;
    }

    public void setExpression(final String expression) {
        this.expression = expression;
    }

    public ExpressionType getExpressionType() {
        return expressionType;
    }

    public void setExpressionType(final ExpressionType expressionType) {
        this.expressionType = expressionType;
    }
}
