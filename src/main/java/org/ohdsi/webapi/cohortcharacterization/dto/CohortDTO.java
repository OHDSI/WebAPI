package org.ohdsi.webapi.cohortcharacterization.dto;

import org.ohdsi.analysis.Cohort;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.webapi.cohortdefinition.ExpressionType;

public class CohortDTO implements Cohort {
    
    private Integer id;
    private String name;
    private String description;
    private CohortExpression expression;
    private ExpressionType expressionType;
    
    public CohortExpression getExpression() {
        return expression;
    }

    public void setExpression(final CohortExpression expression) {
        this.expression = expression;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public ExpressionType getExpressionType() {
        return expressionType;
    }

    public void setExpressionType(final ExpressionType expressionType) {
        this.expressionType = expressionType;
    }
}
