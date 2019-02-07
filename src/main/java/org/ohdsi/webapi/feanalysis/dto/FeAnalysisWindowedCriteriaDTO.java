package org.ohdsi.webapi.feanalysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.ohdsi.circe.cohortdefinition.WindowedCriteria;

public class FeAnalysisWindowedCriteriaDTO extends BaseFeAnalysisCriteriaDTO {

  @JsonProperty("expression")
  private WindowedCriteria expression;

  public FeAnalysisWindowedCriteriaDTO() {
  }

  public FeAnalysisWindowedCriteriaDTO(Long id, String name, WindowedCriteria expression) {
    super(id, name);
    this.expression = expression;
  }

  public WindowedCriteria getExpression() {
    return expression;
  }

  public void setExpression(WindowedCriteria expression) {
    this.expression = expression;
  }
}
