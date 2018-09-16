package org.ohdsi.webapi.prediction.specification;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;

public class TargetOutcomes {
  @JsonProperty("targetId")
  private String targetId = null;

  @JsonProperty("outcomeIds")
  private List<String> outcomeIds = new ArrayList<String>();

  public TargetOutcomes targetId(String targetId) {
    this.targetId = targetId;
    return this;
  }

  /**
   * Target cohort id
   * @return targetId
   **/
  @JsonProperty("targetId")
  @NotNull
  public String getTargetId() {
    return targetId;
  }

  public void setTargetId(String targetId) {
    this.targetId = targetId;
  }

  public TargetOutcomes outcomeIds(List<String> outcomeIds) {
    this.outcomeIds = outcomeIds;
    return this;
  }

  public TargetOutcomes addOutcomeIdsItem(String outcomeIdsItem) {
    this.outcomeIds.add(outcomeIdsItem);
    return this;
  }

  /**
   * The list of outcome cohort ids
   * @return outcomeIds
   **/
  @JsonProperty("outcomeIds")
  @NotNull
  public List<String> getOutcomeIds() {
    return outcomeIds;
  }

  public void setOutcomeIds(List<String> outcomeIds) {
    this.outcomeIds = outcomeIds;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TargetOutcomes targetOutcomes = (TargetOutcomes) o;
    return Objects.equals(this.targetId, targetOutcomes.targetId) &&
        Objects.equals(this.outcomeIds, targetOutcomes.outcomeIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(targetId, outcomeIds);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TargetOutcomes {\n");
    
    sb.append("    targetId: ").append(toIndentedString(targetId)).append("\n");
    sb.append("    outcomeIds: ").append(toIndentedString(outcomeIds)).append("\n");
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
