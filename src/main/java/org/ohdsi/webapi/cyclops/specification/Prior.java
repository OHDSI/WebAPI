package org.ohdsi.webapi.cyclops.specification;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.ArrayList;
import java.util.List;

public class Prior {
  /**
   * Specifies prior distribution. We specify all priors in terms of their variance parameters. Similar fitting tools for regularized regression often parameterize the Laplace distribution in terms of a rate \&quot;lambda\&quot; per observation. See \&quot;glmnet\&quot;, for example.    variance &#x3D; 2 * / (nobs * lambda)^2 or lambda &#x3D; sqrt(2 / variance) / nobs 
   */
  public enum PriorTypeEnum {
    NONE("none"),
    
    LAPLACE("laplace"),
    
    NORMAL("normal");

    private String value;

    PriorTypeEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static PriorTypeEnum fromValue(String text) {
      for (PriorTypeEnum b : PriorTypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  @JsonProperty("priorType")
  private PriorTypeEnum priorType = PriorTypeEnum.LAPLACE;

  @JsonProperty("variance")
  private Integer variance = null;

  @JsonProperty("exclude")
  private Integer exclude = null;

  @JsonProperty("graph")
  private String graph = null;

  @JsonProperty("neighborhood")
  private String neighborhood = null;

  @JsonProperty("useCrossValidation")
  private Boolean useCrossValidation = false;

  @JsonProperty("forceIntercept")
  private Boolean forceIntercept = false;

  @JsonProperty("attr_class")
  private String attrClass = "cyclopsPrior";

  public Prior priorType(PriorTypeEnum priorType) {
    this.priorType = priorType;
    return this;
  }

  /**
   * Specifies prior distribution. We specify all priors in terms of their variance parameters. Similar fitting tools for regularized regression often parameterize the Laplace distribution in terms of a rate \&quot;lambda\&quot; per observation. See \&quot;glmnet\&quot;, for example.    variance &#x3D; 2 * / (nobs * lambda)^2 or lambda &#x3D; sqrt(2 / variance) / nobs 
   * @return priorType
   **/
  @JsonProperty("priorType")
  public PriorTypeEnum getPriorType() {
    return priorType;
  }

  public void setPriorType(PriorTypeEnum priorType) {
    this.priorType = priorType;
  }

  public Prior variance(Integer variance) {
    this.variance = variance;
    return this;
  }

  /**
   * prior distribution variance 
   * @return variance
   **/
  @JsonProperty("variance")
  public Integer getVariance() {
    return variance;
  }

  public void setVariance(Integer variance) {
    this.variance = variance;
  }

  public Prior exclude(Integer exclude) {
    this.exclude = exclude;
    return this;
  }

  /**
   * A vector of numbers or covariateId names to exclude from prior 
   * @return exclude
   **/
  @JsonProperty("exclude")
  public Integer getExclude() {
    return exclude;
  }

  public void setExclude(Integer exclude) {
    this.exclude = exclude;
  }

  public Prior graph(String graph) {
    this.graph = graph;
    return this;
  }

  /**
   * Child-to-parent mapping for a hierarchical prior             
   * @return graph
   **/
  @JsonProperty("graph")
  public String getGraph() {
    return graph;
  }

  public void setGraph(String graph) {
    this.graph = graph;
  }

  public Prior neighborhood(String neighborhood) {
    this.neighborhood = neighborhood;
    return this;
  }

  /**
   * A list of first-order neighborhoods for a partially fused prior 
   * @return neighborhood
   **/
  @JsonProperty("neighborhood")
  public String getNeighborhood() {
    return neighborhood;
  }

  public void setNeighborhood(String neighborhood) {
    this.neighborhood = neighborhood;
  }

  public Prior useCrossValidation(Boolean useCrossValidation) {
    this.useCrossValidation = useCrossValidation;
    return this;
  }

  /**
   * Perform cross-validation to determine prior variance. 
   * @return useCrossValidation
   **/
  @JsonProperty("useCrossValidation")
  public Boolean isisUseCrossValidation() {
    return useCrossValidation;
  }

  public void setUseCrossValidation(Boolean useCrossValidation) {
    this.useCrossValidation = useCrossValidation;
  }

  public Prior forceIntercept(Boolean forceIntercept) {
    this.forceIntercept = forceIntercept;
    return this;
  }

  /**
   * Force intercept coefficient into prior 
   * @return forceIntercept
   **/
  @JsonProperty("forceIntercept")
  public Boolean isisForceIntercept() {
    return forceIntercept;
  }

  public void setForceIntercept(Boolean forceIntercept) {
    this.forceIntercept = forceIntercept;
  }

  public Prior attrClass(String attrClass) {
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
    Prior prior = (Prior) o;
    return Objects.equals(this.priorType, prior.priorType) &&
        Objects.equals(this.variance, prior.variance) &&
        Objects.equals(this.exclude, prior.exclude) &&
        Objects.equals(this.graph, prior.graph) &&
        Objects.equals(this.neighborhood, prior.neighborhood) &&
        Objects.equals(this.useCrossValidation, prior.useCrossValidation) &&
        Objects.equals(this.forceIntercept, prior.forceIntercept) &&
        Objects.equals(this.attrClass, prior.attrClass);
  }

  @Override
  public int hashCode() {
    return Objects.hash(priorType, variance, exclude, graph, neighborhood, useCrossValidation, forceIntercept, attrClass);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Prior {\n");
    
    sb.append("    priorType: ").append(toIndentedString(priorType)).append("\n");
    sb.append("    variance: ").append(toIndentedString(variance)).append("\n");
    sb.append("    exclude: ").append(toIndentedString(exclude)).append("\n");
    sb.append("    graph: ").append(toIndentedString(graph)).append("\n");
    sb.append("    neighborhood: ").append(toIndentedString(neighborhood)).append("\n");
    sb.append("    useCrossValidation: ").append(toIndentedString(useCrossValidation)).append("\n");
    sb.append("    forceIntercept: ").append(toIndentedString(forceIntercept)).append("\n");
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
