package org.ohdsi.webapi.feanalysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.ohdsi.circe.cohortdefinition.CriteriaGroup;

public class FeAnalysisCriteriaDTO {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("expression")
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
