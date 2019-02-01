package org.ohdsi.webapi.estimation.specification;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Analysis {
  @JsonProperty("analysisId")
  private Integer analysisId = null;

  @JsonProperty("description")
  private String description = null;

  public Analysis analysisId(Integer analysisId) {
    this.analysisId = analysisId;
    return this;
  }

  /**
   * Unique identifier for the analysis
   * @return analysisId
   **/
  @JsonProperty("analysisId")
  public Integer getAnalysisId() {
    return analysisId;
  }

  public void setAnalysisId(Integer analysisId) {
    this.analysisId = analysisId;
  }

  public Analysis description(String description) {
    this.description = description;
    return this;
  }

  /**
   * Description of the analysis
   * @return description
   **/
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Analysis analysis = (Analysis) o;
    return Objects.equals(this.analysisId, analysis.analysisId) &&
        Objects.equals(this.description, analysis.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(analysisId, description);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Analysis {\n");
    
    sb.append("    analysisId: ").append(toIndentedString(analysisId)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
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
