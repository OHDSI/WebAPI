package org.ohdsi.webapi.estimation.specification;

import org.ohdsi.webapi.RLangClassImpl;
import org.ohdsi.analysis.estimation.design.Analysis;

public class AnalysisImpl extends RLangClassImpl implements Analysis {
  private Integer analysisId = null;
  private String description = null;

  /**
   * Unique identifier for the analysis
   * @return analysisId
   **/
  @Override
  public Integer getAnalysisId() {
    return analysisId;
  }

  public void setAnalysisId(Integer analysisId) {
    this.analysisId = analysisId;
  }

  /**
   * Description of the analysis
   * @return description
   **/
  @Override
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
