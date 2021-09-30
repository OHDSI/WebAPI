package org.ohdsi.webapi.estimation.comparativecohortanalysis.specification;

import org.ohdsi.analysis.estimation.comparativecohortanalysis.design.OutcomeModelTypeEnum;
import org.ohdsi.analysis.estimation.comparativecohortanalysis.design.FitOutcomeModelArgs;
import org.ohdsi.webapi.RLangClassImpl;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;
import org.ohdsi.webapi.cyclops.specification.ControlImpl;
import org.ohdsi.webapi.cyclops.specification.PriorImpl;

/**
 *
 * @author asena5
 */
public class FitOutcomeModelArgsImpl extends RLangClassImpl implements FitOutcomeModelArgs {
  private OutcomeModelTypeEnum modelType = OutcomeModelTypeEnum.COX;
  private Boolean stratified = true;
  private Boolean useCovariates = true;
  private Boolean inversePtWeighting = false;
  private List<Integer> interactionCovariateIds = null;
  private List<Integer> excludeCovariateIds = null;
  private List<Integer> includeCovariateIds = null;
  private PriorImpl prior = null;
  private ControlImpl control = null;

  /**
   * The type of outcome model that will be used. Possible values are \&quot;logistic\&quot;, \&quot;poisson\&quot;, or \&quot;cox\&quot;. 
   * @return modelType
   **/
  @Override
  @NotNull
  public OutcomeModelTypeEnum getModelType() {
    return modelType;
  }

    /**
     *
     * @param modelType
     */
    public void setModelType(OutcomeModelTypeEnum modelType) {
    this.modelType = modelType;
  }

  /**
   * Should the regression be conditioned on the strata defined in the population object (e.g. by matching or stratifying on propensity scores)? 
   * @return stratified
   **/
  @Override
  public Boolean getStratified() {
    return stratified;
  }

    /**
     *
     * @param stratified
     */
    public void setStratified(Boolean stratified) {
    this.stratified = stratified;
  }

  /**
   * Whether to use the covariate matrix in the cohortMethodDataobject in the outcome model. 
   * @return useCovariates
   **/
  @Override
  public Boolean getUseCovariates() {
    return useCovariates;
  }

    /**
     *
     * @param useCovariates
     */
    public void setUseCovariates(Boolean useCovariates) {
    this.useCovariates = useCovariates;
  }

  /**
   * Use inverse probability of treatment weighting? 
   * @return inversePtWeighting
   **/
  @Override
  public Boolean getInversePtWeighting() {
    return inversePtWeighting;
  }

    /**
     *
     * @param inversePtWeighting
     */
    public void setInversePtWeighting(Boolean inversePtWeighting) {
    this.inversePtWeighting = inversePtWeighting;
  }

    /**
     *
     * @param interactionCovariateIdsItem
     * @return
     */
    public FitOutcomeModelArgsImpl addInteractionCovariateIdsItem(Integer interactionCovariateIdsItem) {
    if (this.interactionCovariateIds == null) {
      this.interactionCovariateIds = new ArrayList<>();
    }
    this.interactionCovariateIds.add(interactionCovariateIdsItem);
    return this;
  }

  /**
   * An optional vector of covariate IDs to use to estimate interactions with the main treatment effect. 
   * @return interactionCovariateIds
   **/
  @Override
  public List<Integer> getInteractionCovariateIds() {
    return interactionCovariateIds;
  }

    /**
     *
     * @param interactionCovariateIds
     */
    public void setInteractionCovariateIds(List<Integer> interactionCovariateIds) {
    this.interactionCovariateIds = interactionCovariateIds;
  }

    /**
     *
     * @param excludeCovariateIdsItem
     * @return
     */
    public FitOutcomeModelArgsImpl addExcludeCovariateIdsItem(Integer excludeCovariateIdsItem) {
    if (this.excludeCovariateIds == null) {
      this.excludeCovariateIds = new ArrayList<>();
    }
    this.excludeCovariateIds.add(excludeCovariateIdsItem);
    return this;
  }

  /**
   * Exclude these covariates from the outcome model. 
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
     *
     * @param includeCovariateIdsItem
     * @return
     */
    public FitOutcomeModelArgsImpl addIncludeCovariateIdsItem(Integer includeCovariateIdsItem) {
    if (this.includeCovariateIds == null) {
      this.includeCovariateIds = new ArrayList<>();
    }
    this.includeCovariateIds.add(includeCovariateIdsItem);
    return this;
  }

  /**
   * Include only these covariates in the outcome model. 
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
}
