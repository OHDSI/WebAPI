package org.ohdsi.webapi.prediction.specification;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ConceptSetCrossReference {
  @JsonProperty("conceptSetId")
  private Integer conceptSetId = null;

  @JsonProperty("targetName")
  private String targetName = null;

  @JsonProperty("targetIndex")
  private Integer targetIndex = 0;

  @JsonProperty("propertyName")
  private String propertyName = null;

  public ConceptSetCrossReference conceptSetId(Integer conceptSetId) {
    this.conceptSetId = conceptSetId;
    return this;
  }

  /**
   * The concept set ID
   * @return conceptSetId
   **/
  @JsonProperty("conceptSetId")
  public Integer getConceptSetId() {
    return conceptSetId;
  }

  public void setConceptSetId(Integer conceptSetId) {
    this.conceptSetId = conceptSetId;
  }

  public ConceptSetCrossReference targetName(String targetName) {
    this.targetName = targetName;
    return this;
  }

  /**
   * The target object name that will utilize the concept set
   * @return targetName
   **/
  @JsonProperty("targetName")
  public String getTargetName() {
    return targetName;
  }

  public void setTargetName(String targetName) {
    this.targetName = targetName;
  }

  public ConceptSetCrossReference targetIndex(Integer targetIndex) {
    this.targetIndex = targetIndex;
    return this;
  }

  /**
   * The index of the target object
   * @return targetIndex
   **/
  @JsonProperty("targetIndex")
  public Integer getTargetIndex() {
    return targetIndex;
  }

  public void setTargetIndex(Integer targetIndex) {
    this.targetIndex = targetIndex;
  }

  public ConceptSetCrossReference propertyName(String propertyName) {
    this.propertyName = propertyName;
    return this;
  }

  /**
   * The property that will hold the list of concept IDs from  the resolved concept set 
   * @return propertyName
   **/
  @JsonProperty("propertyName")
  public String getPropertyName() {
    return propertyName;
  }

  public void setPropertyName(String propertyName) {
    this.propertyName = propertyName;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConceptSetCrossReference conceptSetCrossReference = (ConceptSetCrossReference) o;
    return Objects.equals(this.conceptSetId, conceptSetCrossReference.conceptSetId) &&
        Objects.equals(this.targetName, conceptSetCrossReference.targetName) &&
        Objects.equals(this.targetIndex, conceptSetCrossReference.targetIndex) &&
        Objects.equals(this.propertyName, conceptSetCrossReference.propertyName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(conceptSetId, targetName, targetIndex, propertyName);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConceptSetCrossReference {\n");
    
    sb.append("    conceptSetId: ").append(toIndentedString(conceptSetId)).append("\n");
    sb.append("    targetName: ").append(toIndentedString(targetName)).append("\n");
    sb.append("    targetIndex: ").append(toIndentedString(targetIndex)).append("\n");
    sb.append("    propertyName: ").append(toIndentedString(propertyName)).append("\n");
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
