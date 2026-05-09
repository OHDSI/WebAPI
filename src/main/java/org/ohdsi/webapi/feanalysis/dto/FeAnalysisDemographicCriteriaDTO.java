package org.ohdsi.webapi.feanalysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.ohdsi.circe.cohortdefinition.DemographicCriteria;

public class FeAnalysisDemographicCriteriaDTO extends BaseFeAnalysisCriteriaDTO {

  @JsonProperty("expression")
  private DemographicCriteria expression;

  public FeAnalysisDemographicCriteriaDTO() {
  }

  public FeAnalysisDemographicCriteriaDTO(Long id, String name, DemographicCriteria expression) {
    super(id, name);
    this.expression = expression;
  }

  public DemographicCriteria getExpression() {
    return expression;
  }

  public void setExpression(DemographicCriteria expression) {
    this.expression = expression;
  }
}
