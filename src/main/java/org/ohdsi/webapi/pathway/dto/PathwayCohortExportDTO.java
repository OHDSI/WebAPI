package org.ohdsi.webapi.pathway.dto;

import org.ohdsi.analysis.Cohort;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.webapi.cohortdefinition.ExpressionType;

public class PathwayCohortExportDTO extends PathwayCohortDTO implements Cohort {

    private CohortExpression expression;
    private ExpressionType expressionType;

    public CohortExpression getExpression() {
        return expression;
    }

    public void setExpression(final CohortExpression expression) {
        this.expression = expression;
    }

    public ExpressionType getExpressionType() {
        return expressionType;
    }

    public void setExpressionType(final ExpressionType expressionType) {
        this.expressionType = expressionType;
    }
}
