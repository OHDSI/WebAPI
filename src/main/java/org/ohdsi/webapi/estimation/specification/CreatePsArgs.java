package org.ohdsi.webapi.estimation.specification;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import org.ohdsi.webapi.cyclops.specification.Control;
import org.ohdsi.webapi.cyclops.specification.Prior;

public class CreatePsArgs {
  @JsonProperty("excludeCovariateIds")
  private List<Integer> excludeCovariateIds = null;

  @JsonProperty("includeCovariateIds")
  private List<Integer> includeCovariateIds = null;

  @JsonProperty("maxCohortSizeForFitting")
  private Integer maxCohortSizeForFitting = 250000;

  @JsonProperty("errorOnHighCorrelation")
  private Boolean errorOnHighCorrelation = true;

  @JsonProperty("stopOnError")
  private Boolean stopOnError = true;

  @JsonProperty("prior")
  private Prior prior = null;

  @JsonProperty("control")
  private Control control = null;

  @JsonProperty("attr_class")
  private String attrClass = "args";

  public CreatePsArgs excludeCovariateIds(List<Integer> excludeCovariateIds) {
    this.excludeCovariateIds = excludeCovariateIds;
    return this;
  }

  public CreatePsArgs addExcludeCovariateIdsItem(Integer excludeCovariateIdsItem) {
    if (this.excludeCovariateIds == null) {
      this.excludeCovariateIds = new ArrayList<Integer>();
    }
    this.excludeCovariateIds.add(excludeCovariateIdsItem);
    return this;
  }

  /**
   * Exclude these covariates from the propensity model. 
   * @return excludeCovariateIds
   **/
  @JsonProperty("excludeCovariateIds")
  public List<Integer> getExcludeCovariateIds() {
    return excludeCovariateIds;
  }

  public void setExcludeCovariateIds(List<Integer> excludeCovariateIds) {
    this.excludeCovariateIds = excludeCovariateIds;
  }

  public CreatePsArgs includeCovariateIds(List<Integer> includeCovariateIds) {
    this.includeCovariateIds = includeCovariateIds;
    return this;
  }

  public CreatePsArgs addIncludeCovariateIdsItem(Integer includeCovariateIdsItem) {
    if (this.includeCovariateIds == null) {
      this.includeCovariateIds = new ArrayList<Integer>();
    }
    this.includeCovariateIds.add(includeCovariateIdsItem);
    return this;
  }

  /**
   * Include only these covariates in the propensity model. 
   * @return includeCovariateIds
   **/
  @JsonProperty("includeCovariateIds")
  public List<Integer> getIncludeCovariateIds() {
    return includeCovariateIds;
  }

  public void setIncludeCovariateIds(List<Integer> includeCovariateIds) {
    this.includeCovariateIds = includeCovariateIds;
  }

  public CreatePsArgs maxCohortSizeForFitting(Integer maxCohortSizeForFitting) {
    this.maxCohortSizeForFitting = maxCohortSizeForFitting;
    return this;
  }

  /**
   * If the target or comparator cohort are larger than this number, they will be down-sampled before fitting the propensity model. The model will be used to compute propensity scores for all subjects. The purpose of the sampling is to gain speed. Setting this number to 0 means no down-sampling will be applied.           
   * @return maxCohortSizeForFitting
   **/
  @JsonProperty("maxCohortSizeForFitting")
  public Integer getMaxCohortSizeForFitting() {
    return maxCohortSizeForFitting;
  }

  public void setMaxCohortSizeForFitting(Integer maxCohortSizeForFitting) {
    this.maxCohortSizeForFitting = maxCohortSizeForFitting;
  }

  public CreatePsArgs errorOnHighCorrelation(Boolean errorOnHighCorrelation) {
    this.errorOnHighCorrelation = errorOnHighCorrelation;
    return this;
  }

  /**
   * If true, the function will test each covariate for correlation with the target assignment. If any covariate has an unusually high correlation (either positive or negative), this will throw an error. 
   * @return errorOnHighCorrelation
   **/
  @JsonProperty("errorOnHighCorrelation")
  public Boolean isisErrorOnHighCorrelation() {
    return errorOnHighCorrelation;
  }

  public void setErrorOnHighCorrelation(Boolean errorOnHighCorrelation) {
    this.errorOnHighCorrelation = errorOnHighCorrelation;
  }

  public CreatePsArgs stopOnError(Boolean stopOnError) {
    this.stopOnError = stopOnError;
    return this;
  }

  /**
   * If an error occurs, should the function stop? Else, the two cohorts will be assumed to be perfectly separable. 
   * @return stopOnError
   **/
  @JsonProperty("stopOnError")
  public Boolean isisStopOnError() {
    return stopOnError;
  }

  public void setStopOnError(Boolean stopOnError) {
    this.stopOnError = stopOnError;
  }

  public CreatePsArgs prior(Prior prior) {
    this.prior = prior;
    return this;
  }

  /**
   * Get prior
   * @return prior
   **/
  @JsonProperty("prior")
  public Prior getPrior() {
    return prior;
  }

  public void setPrior(Prior prior) {
    this.prior = prior;
  }

  public CreatePsArgs control(Control control) {
    this.control = control;
    return this;
  }

  /**
   * Get control
   * @return control
   **/
  @JsonProperty("control")
  public Control getControl() {
    return control;
  }

  public void setControl(Control control) {
    this.control = control;
  }

  public CreatePsArgs attrClass(String attrClass) {
    this.attrClass = attrClass;
    return this;
  }

  /**
   * Get attrClass
   * @return attrClass
   **/
  @JsonProperty("attr_class")
  public String getAttrClass() {
    return attrClass;
  }

  public void setAttrClass(String attrClass) {
    this.attrClass = attrClass;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreatePsArgs createPsArgs = (CreatePsArgs) o;
    return Objects.equals(this.excludeCovariateIds, createPsArgs.excludeCovariateIds) &&
        Objects.equals(this.includeCovariateIds, createPsArgs.includeCovariateIds) &&
        Objects.equals(this.maxCohortSizeForFitting, createPsArgs.maxCohortSizeForFitting) &&
        Objects.equals(this.errorOnHighCorrelation, createPsArgs.errorOnHighCorrelation) &&
        Objects.equals(this.stopOnError, createPsArgs.stopOnError) &&
        Objects.equals(this.prior, createPsArgs.prior) &&
        Objects.equals(this.control, createPsArgs.control) &&
        Objects.equals(this.attrClass, createPsArgs.attrClass);
  }

  @Override
  public int hashCode() {
    return Objects.hash(excludeCovariateIds, includeCovariateIds, maxCohortSizeForFitting, errorOnHighCorrelation, stopOnError, prior, control, attrClass);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreatePsArgs {\n");
    
    sb.append("    excludeCovariateIds: ").append(toIndentedString(excludeCovariateIds)).append("\n");
    sb.append("    includeCovariateIds: ").append(toIndentedString(includeCovariateIds)).append("\n");
    sb.append("    maxCohortSizeForFitting: ").append(toIndentedString(maxCohortSizeForFitting)).append("\n");
    sb.append("    errorOnHighCorrelation: ").append(toIndentedString(errorOnHighCorrelation)).append("\n");
    sb.append("    stopOnError: ").append(toIndentedString(stopOnError)).append("\n");
    sb.append("    prior: ").append(toIndentedString(prior)).append("\n");
    sb.append("    control: ").append(toIndentedString(control)).append("\n");
    sb.append("    attrClass: ").append(toIndentedString(attrClass)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }    
}
