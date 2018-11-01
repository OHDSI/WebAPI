package org.ohdsi.webapi.cyclops.specification;

import org.ohdsi.analysis.cyclops.design.*;
import org.ohdsi.webapi.RLangClassImpl;

public class PriorImpl extends RLangClassImpl implements Prior {
  private PriorTypeEnum priorType = PriorTypeEnum.LAPLACE;
  private Integer variance = null;
  private Integer exclude = null;
  private String graph = null;
  private String neighborhood = null;
  private Boolean useCrossValidation = false;
  private Boolean forceIntercept = false;
  private String priorAttrClass = "cyclopsPrior";

  /**
   * Specifies prior distribution. We specify all priors in terms of their variance parameters. Similar fitting tools for regularized regression often parameterize the Laplace distribution in terms of a rate \&quot;lambda\&quot; per observation. See \&quot;glmnet\&quot;, for example.    variance &#x3D; 2 * / (nobs * lambda)^2 or lambda &#x3D; sqrt(2 / variance) / nobs 
   * @return priorType
   **/
  @Override
  public PriorTypeEnum getPriorType() {
    return priorType;
  }

  public void setPriorType(PriorTypeEnum priorType) {
    this.priorType = priorType;
  }

  /**
   * prior distribution variance 
   * @return variance
   **/
  @Override
  public Integer getVariance() {
    return variance;
  }

  public void setVariance(Integer variance) {
    this.variance = variance;
  }

  /**
   * A vector of numbers or covariateId names to exclude from prior 
   * @return exclude
   **/
  @Override
  public Integer getExclude() {
    return exclude;
  }

  public void setExclude(Integer exclude) {
    this.exclude = exclude;
  }

  /**
   * Child-to-parent mapping for a hierarchical prior             
   * @return graph
   **/
  @Override
  public String getGraph() {
    return graph;
  }

  public void setGraph(String graph) {
    this.graph = graph;
  }

  /**
   * A list of first-order neighborhoods for a partially fused prior 
   * @return neighborhood
   **/
  @Override
  public String getNeighborhood() {
    return neighborhood;
  }

  public void setNeighborhood(String neighborhood) {
    this.neighborhood = neighborhood;
  }

  /**
   * Perform cross-validation to determine prior variance. 
   * @return useCrossValidation
   **/
  @Override
  public Boolean getUseCrossValidation() {
    return useCrossValidation;
  }

  public void setUseCrossValidation(Boolean useCrossValidation) {
    this.useCrossValidation = useCrossValidation;
  }

  /**
   * Force intercept coefficient into prior 
   * @return forceIntercept
   **/
  @Override
  public Boolean getForceIntercept() {
    return forceIntercept;
  }

  public void setForceIntercept(Boolean forceIntercept) {
    this.forceIntercept = forceIntercept;
  }

  /**
   * Get priorAttrClass
   * @return priorAttrClass
   **/
  @Override
  public String getAttrClass() {
    return priorAttrClass;
  }

  @Override
  public void setAttrClass(String attrClass) {
    this.priorAttrClass = attrClass;
  }
}
