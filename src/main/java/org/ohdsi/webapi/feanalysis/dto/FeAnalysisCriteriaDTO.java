package org.ohdsi.webapi.feanalysis.dto;

import org.ohdsi.circe.cohortdefinition.CriteriaGroup;

public class FeAnalysisCriteriaDTO {
    private String name;
    private CriteriaGroup expression;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public CriteriaGroup getExpression() {
        return expression;
    }

    public void setExpression(final CriteriaGroup expression) {
        this.expression = expression;
    }
}
