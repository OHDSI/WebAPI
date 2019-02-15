package org.ohdsi.webapi.estimation.comparativecohortanalysis.specification;

import org.ohdsi.webapi.RLangClassImpl;
import java.util.List;
import org.ohdsi.webapi.cyclops.specification.ControlImpl;
import org.ohdsi.webapi.cyclops.specification.PriorImpl;
import org.ohdsi.analysis.estimation.comparativecohortanalysis.design.CreatePsArgs;

/**
 *
 * @author asena5
 */
public class CreatePsArgsImpl extends RLangClassImpl implements CreatePsArgs {
  private List<Integer> excludeCovariateIds = null;
  private List<Integer> includeCovariateIds = null;
  private Integer maxCohortSizeForFitting = 250000;
  private Boolean errorOnHighCorrelation = true;
  private Boolean stopOnError = true;
  private PriorImpl prior = null;
  private ControlImpl control = null;

  /**
   * Exclude these covariates from the propensity model. 
   * @return excludeCovariateIds
   **/
  @Override
  public List<Integer> getExcludeCovariateIds() {
    return excludeCovariateIds;
  }

    /**
     *
     * @param excludeCovariateIds
     */
    public void setExcludeCovariateIds(List<Integer> excludeCovariateIds) {
    this.excludeCovariateIds = excludeCovariateIds;
  }

  /**
   * Include only these covariates in the propensity model. 
   * @return includeCovariateIds
   **/
  @Override
  public List<Integer> getIncludeCovariateIds() {
    return includeCovariateIds;
  }

    /**
     *
     * @param includeCovariateIds
     */
    public void setIncludeCovariateIds(List<Integer> includeCovariateIds) {
    this.includeCovariateIds = includeCovariateIds;
  }

  /**
   * If the target or comparator cohort are larger than this number, they will be down-sampled before fitting the propensity model. The model will be used to compute propensity scores for all subjects. The purpose of the sampling is to gain speed. Setting this number to 0 means no down-sampling will be applied.           
   * @return maxCohortSizeForFitting
   **/
  @Override
  public Integer getMaxCohortSizeForFitting() {
    return maxCohortSizeForFitting;
  }

    /**
     *
     * @param maxCohortSizeForFitting
     */
    public void setMaxCohortSizeForFitting(Integer maxCohortSizeForFitting) {
    this.maxCohortSizeForFitting = maxCohortSizeForFitting;
  }

  /**
   * If true, the function will test each covariate for correlation with the target assignment. If any covariate has an unusually high correlation (either positive or negative), this will throw an error. 
   * @return errorOnHighCorrelation
   **/
  @Override
  public Boolean getErrorOnHighCorrelation() {
    return errorOnHighCorrelation;
  }

    /**
     *
     * @param errorOnHighCorrelation
     */
    public void setErrorOnHighCorrelation(Boolean errorOnHighCorrelation) {
    this.errorOnHighCorrelation = errorOnHighCorrelation;
  }

  /**
   * If an error occurs, should the function stop? Else, the two cohorts will be assumed to be perfectly separable. 
   * @return stopOnError
   **/
  @Override
  public Boolean getStopOnError() {
    return stopOnError;
  }

    /**
     *
     * @param stopOnError
     */
    public void setStopOnError(Boolean stopOnError) {
    this.stopOnError = stopOnError;
  }

  /**
   * Get prior
   * @return prior
   **/
  @Override
  public PriorImpl getPrior() {
    return prior;
  }

    /**
     *
     * @param prior
     */
    public void setPrior(PriorImpl prior) {
    this.prior = prior;
  }

  /**
   * Get control
   * @return control
   **/
  @Override
  public ControlImpl getControl() {
    return control;
  }

    /**
     *
     * @param control
     */
    public void setControl(ControlImpl control) {
    this.control = control;
  }

    /**
     *
     * @param attrClass
     * @return
     */
    public CreatePsArgsImpl attrClass(String attrClass) {
    this.attrClass = attrClass;
    return this;
  }
}
