package org.ohdsi.webapi.estimation.specification;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;
import org.ohdsi.webapi.cyclops.specification.Control;
import org.ohdsi.webapi.cyclops.specification.Prior;

public class FitOutcomeModelArgs {
  /**
   * The type of outcome model that will be used. Possible values are \&quot;logistic\&quot;, \&quot;poisson\&quot;, or \&quot;cox\&quot;. 
   */
  public enum ModelTypeEnum {
    LOGISTIC("logistic"),
    
    POISSON("poisson"),
    
    COX("cox");

    private String value;

    ModelTypeEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static ModelTypeEnum fromValue(String text) {
      for (ModelTypeEnum b : ModelTypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  @JsonProperty("modelType")
  private ModelTypeEnum modelType = ModelTypeEnum.COX;

  @JsonProperty("stratified")
  private Boolean stratified = true;

  @JsonProperty("useCovariates")
  private Boolean useCovariates = true;

  @JsonProperty("inversePtWeighting")
  private Boolean inversePtWeighting = false;

  @JsonProperty("interactionCovariateIds")
  private List<Integer> interactionCovariateIds = null;

  @JsonProperty("excludeCovariateIds")
  private List<Integer> excludeCovariateIds = null;

  @JsonProperty("includeCovariateIds")
  private List<Integer> includeCovariateIds = null;

  @JsonProperty("prior")
  private Prior prior = null;

  @JsonProperty("control")
  private Control control = null;

  @JsonProperty("attr_class")
  private String attrClass = "args";

  public FitOutcomeModelArgs modelType(ModelTypeEnum modelType) {
    this.modelType = modelType;
    return this;
  }

  /**
   * The type of outcome model that will be used. Possible values are \&quot;logistic\&quot;, \&quot;poisson\&quot;, or \&quot;cox\&quot;. 
   * @return modelType
   **/
  @JsonProperty("modelType")
  @NotNull
  public ModelTypeEnum getModelType() {
    return modelType;
  }

  public void setModelType(ModelTypeEnum modelType) {
    this.modelType = modelType;
  }

  public FitOutcomeModelArgs stratified(Boolean stratified) {
    this.stratified = stratified;
    return this;
  }

  /**
   * Should the regression be conditioned on the strata defined in the population object (e.g. by matching or stratifying on propensity scores)? 
   * @return stratified
   **/
  @JsonProperty("stratified")
  public Boolean isisStratified() {
    return stratified;
  }

  public void setStratified(Boolean stratified) {
    this.stratified = stratified;
  }

  public FitOutcomeModelArgs useCovariates(Boolean useCovariates) {
    this.useCovariates = useCovariates;
    return this;
  }

  /**
   * Whether to use the covariate matrix in the cohortMethodDataobject in the outcome model. 
   * @return useCovariates
   **/
  @JsonProperty("useCovariates")
  public Boolean isisUseCovariates() {
    return useCovariates;
  }

  public void setUseCovariates(Boolean useCovariates) {
    this.useCovariates = useCovariates;
  }

  public FitOutcomeModelArgs inversePtWeighting(Boolean inversePtWeighting) {
    this.inversePtWeighting = inversePtWeighting;
    return this;
  }

  /**
   * Use inverse probability of treatment weighting? 
   * @return inversePtWeighting
   **/
  @JsonProperty("inversePtWeighting")
  public Boolean isisInversePtWeighting() {
    return inversePtWeighting;
  }

  public void setInversePtWeighting(Boolean inversePtWeighting) {
    this.inversePtWeighting = inversePtWeighting;
  }

  public FitOutcomeModelArgs interactionCovariateIds(List<Integer> interactionCovariateIds) {
    this.interactionCovariateIds = interactionCovariateIds;
    return this;
  }

  public FitOutcomeModelArgs addInteractionCovariateIdsItem(Integer interactionCovariateIdsItem) {
    if (this.interactionCovariateIds == null) {
      this.interactionCovariateIds = new ArrayList<Integer>();
    }
    this.interactionCovariateIds.add(interactionCovariateIdsItem);
    return this;
  }

  /**
   * An optional vector of covariate IDs to use to estimate interactions with the main treatment effect. 
   * @return interactionCovariateIds
   **/
  @JsonProperty("interactionCovariateIds")
  public List<Integer> getInteractionCovariateIds() {
    return interactionCovariateIds;
  }

  public void setInteractionCovariateIds(List<Integer> interactionCovariateIds) {
    this.interactionCovariateIds = interactionCovariateIds;
  }

  public FitOutcomeModelArgs excludeCovariateIds(List<Integer> excludeCovariateIds) {
    this.excludeCovariateIds = excludeCovariateIds;
    return this;
  }

  public FitOutcomeModelArgs addExcludeCovariateIdsItem(Integer excludeCovariateIdsItem) {
    if (this.excludeCovariateIds == null) {
      this.excludeCovariateIds = new ArrayList<Integer>();
    }
    this.excludeCovariateIds.add(excludeCovariateIdsItem);
    return this;
  }

  /**
   * Exclude these covariates from the outcome model. 
   * @return excludeCovariateIds
   **/
  @JsonProperty("excludeCovariateIds")
  public List<Integer> getExcludeCovariateIds() {
    return excludeCovariateIds;
  }

  public void setExcludeCovariateIds(List<Integer> excludeCovariateIds) {
    this.excludeCovariateIds = excludeCovariateIds;
  }

  public FitOutcomeModelArgs includeCovariateIds(List<Integer> includeCovariateIds) {
    this.includeCovariateIds = includeCovariateIds;
    return this;
  }

  public FitOutcomeModelArgs addIncludeCovariateIdsItem(Integer includeCovariateIdsItem) {
    if (this.includeCovariateIds == null) {
      this.includeCovariateIds = new ArrayList<Integer>();
    }
    this.includeCovariateIds.add(includeCovariateIdsItem);
    return this;
  }

  /**
   * Include only these covariates in the outcome model. 
   * @return includeCovariateIds
   **/
  @JsonProperty("includeCovariateIds")
  public List<Integer> getIncludeCovariateIds() {
    return includeCovariateIds;
  }

  public void setIncludeCovariateIds(List<Integer> includeCovariateIds) {
    this.includeCovariateIds = includeCovariateIds;
  }

  public FitOutcomeModelArgs prior(Prior prior) {
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

  public FitOutcomeModelArgs control(Control control) {
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

  public FitOutcomeModelArgs attrClass(String attrClass) {
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
    FitOutcomeModelArgs fitOutcomeModelArgs = (FitOutcomeModelArgs) o;
    return Objects.equals(this.modelType, fitOutcomeModelArgs.modelType) &&
        Objects.equals(this.stratified, fitOutcomeModelArgs.stratified) &&
        Objects.equals(this.useCovariates, fitOutcomeModelArgs.useCovariates) &&
        Objects.equals(this.inversePtWeighting, fitOutcomeModelArgs.inversePtWeighting) &&
        Objects.equals(this.interactionCovariateIds, fitOutcomeModelArgs.interactionCovariateIds) &&
        Objects.equals(this.excludeCovariateIds, fitOutcomeModelArgs.excludeCovariateIds) &&
        Objects.equals(this.includeCovariateIds, fitOutcomeModelArgs.includeCovariateIds) &&
        Objects.equals(this.prior, fitOutcomeModelArgs.prior) &&
        Objects.equals(this.control, fitOutcomeModelArgs.control) &&
        Objects.equals(this.attrClass, fitOutcomeModelArgs.attrClass);
  }

  @Override
  public int hashCode() {
    return Objects.hash(modelType, stratified, useCovariates, inversePtWeighting, interactionCovariateIds, excludeCovariateIds, includeCovariateIds, prior, control, attrClass);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FitOutcomeModelArgs {\n");
    
    sb.append("    modelType: ").append(toIndentedString(modelType)).append("\n");
    sb.append("    stratified: ").append(toIndentedString(stratified)).append("\n");
    sb.append("    useCovariates: ").append(toIndentedString(useCovariates)).append("\n");
    sb.append("    inversePtWeighting: ").append(toIndentedString(inversePtWeighting)).append("\n");
    sb.append("    interactionCovariateIds: ").append(toIndentedString(interactionCovariateIds)).append("\n");
    sb.append("    excludeCovariateIds: ").append(toIndentedString(excludeCovariateIds)).append("\n");
    sb.append("    includeCovariateIds: ").append(toIndentedString(includeCovariateIds)).append("\n");
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
