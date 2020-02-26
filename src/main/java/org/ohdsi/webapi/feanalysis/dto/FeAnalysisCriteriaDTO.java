package org.ohdsi.webapi.feanalysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.ohdsi.circe.cohortdefinition.CriteriaGroup;

public class FeAnalysisCriteriaDTO extends BaseFeAnalysisCriteriaDTO {
    @JsonProperty("expression")
    private CriteriaGroup expression;

    @JsonProperty("aggregate")
    private FeAnalysisAggregateDTO aggregate;

    public FeAnalysisCriteriaDTO() {

    }

    public FeAnalysisCriteriaDTO(Long id, String name, CriteriaGroup expression) {

        super(id, name);
        this.expression = expression;
    }

    public CriteriaGroup getExpression() {
        return expression;
    }

    public void setExpression(final CriteriaGroup expression) {
        this.expression = expression;
    }

    @Override
    public FeAnalysisAggregateDTO getAggregate() {
        return aggregate;
    }

    @Override
    public void setAggregate(FeAnalysisAggregateDTO aggregate) {
        this.aggregate = aggregate;
    }
}
