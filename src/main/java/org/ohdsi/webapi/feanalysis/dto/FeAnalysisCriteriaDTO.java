package org.ohdsi.webapi.feanalysis.dto;

import org.ohdsi.circe.cohortdefinition.CriteriaGroup;

public class FeAnalysisCriteriaDTO {
    private Long id;
    private String name;
    private CriteriaGroup expression;

    public FeAnalysisCriteriaDTO() {

    }

    public FeAnalysisCriteriaDTO(Long id, String name, CriteriaGroup expression) {

        this.id = id;
        this.name = name;
        this.expression = expression;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
