package org.ohdsi.webapi.estimation.specification;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

public class EstimationAnalysisSettings {
  public enum EstimationTypeEnum {
    COMPARATIVE_COHORT_ANALYSIS("ComparativeCohortAnalysis");

    private String value;

    EstimationTypeEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static EstimationTypeEnum fromValue(String text) {
      for (EstimationTypeEnum b : EstimationTypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
    
  @JsonProperty("estimationType")
  private EstimationTypeEnum estimationType = EstimationTypeEnum.COMPARATIVE_COHORT_ANALYSIS;

  @JsonProperty("analysisSpecification")
  private Object analysisSpecification = null;

  public EstimationAnalysisSettings estimationType(EstimationTypeEnum estimationType) {
    this.estimationType = estimationType;
    return this;
  }

  /**
   * The type of estimation analysis to execute 
   * @return estimationType
   **/
  @JsonProperty("estimationType")
  public EstimationTypeEnum getEstimationType() {
    return estimationType;
  }

  public void setEstimationType(EstimationTypeEnum estimationType) {
    this.estimationType = estimationType;
  }

  public EstimationAnalysisSettings analysisSpecification(Object analysisSpecification) {
    this.analysisSpecification = analysisSpecification;
    return this;
  }

  /**
   * Get analysisSpecification
   * @return analysisSpecification
   **/
  @JsonProperty("analysisSpecification")
  public Object getAnalysisSpecification() {
    return analysisSpecification;
  }

  public void setAnalysisSpecification(Object analysisSpecification) {
    this.analysisSpecification = analysisSpecification;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EstimationAnalysisSettings estimationAnalysisSettings = (EstimationAnalysisSettings) o;
    return Objects.equals(this.estimationType, estimationAnalysisSettings.estimationType) &&
        Objects.equals(this.analysisSpecification, estimationAnalysisSettings.analysisSpecification);
  }

  @Override
  public int hashCode() {
    return Objects.hash(estimationType, analysisSpecification);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EstimationAnalysisSettings {\n");
    
    sb.append("    estimationType: ").append(toIndentedString(estimationType)).append("\n");
    sb.append("    analysisSpecification: ").append(toIndentedString(analysisSpecification)).append("\n");
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
