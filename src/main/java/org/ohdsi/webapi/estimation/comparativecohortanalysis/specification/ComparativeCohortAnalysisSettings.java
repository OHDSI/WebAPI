package org.ohdsi.webapi.estimation.comparativecohortanalysis.specification;

import org.ohdsi.analysis.estimation.design.EstimationTypeEnum;
import org.ohdsi.webapi.estimation.specification.EstimationAnalysisSettingsImpl;

/**
 *
 * @author asena5
 */
public class ComparativeCohortAnalysisSettings extends EstimationAnalysisSettingsImpl {
  private ComparativeCohortAnalysisImpl analysisSpecification = null;

  /**
   * The type of estimation analysis to execute 
   * @return estimationType
   **/
  @Override
  public EstimationTypeEnum getEstimationType() {
    return EstimationTypeEnum.COMPARATIVE_COHORT_ANALYSIS;
  }

  /**
   * Get analysisSpecification
   * @return analysisSpecification
   **/
  @Override
  public ComparativeCohortAnalysisImpl getAnalysisSpecification() {
    return analysisSpecification;
  }

    /**
     *
     * @param analysisSpecification
     */
    public void setAnalysisSpecification(ComparativeCohortAnalysisImpl analysisSpecification) {
    this.analysisSpecification = analysisSpecification;
  } 
}
