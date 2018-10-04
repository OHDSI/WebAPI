package org.ohdsi.webapi.feanalysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.ohdsi.circe.cohortdefinition.ConceptSet;
import org.ohdsi.circe.cohortdefinition.CriteriaGroup;

import java.util.List;

public class FeAnalysisCriteriaDTO {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("expression")
    private CriteriaGroup expression;
    @JsonProperty("conceptSets")
    private List<ConceptSet> conceptSets;

    public FeAnalysisCriteriaDTO() {

    }

    public FeAnalysisCriteriaDTO(Long id, String name, CriteriaGroup expression, List<ConceptSet> conceptSets) {

        this.id = id;
        this.name = name;
        this.expression = expression;
        this.conceptSets = conceptSets;
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

    public List<ConceptSet> getConceptSets() {
        return conceptSets;
    }

    public void setConceptSets(List<ConceptSet> conceptSets) {
        this.conceptSets = conceptSets;
    }
}
