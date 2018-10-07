package org.ohdsi.webapi.estimation.specification;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;

public class TargetOutcome {
  @JsonProperty("targetId")
  private Long targetId = null;

  @JsonProperty("outcomeIds")
  private List<Long> outcomeIds = new ArrayList<Long>();

  public TargetOutcome targetId(Long targetId) {
    this.targetId = targetId;
    return this;
  }

  /**
   * Target cohort id
   * @return targetId
   **/
  @JsonProperty("targetId")
  @NotNull
  public Long getTargetId() {
    return targetId;
  }

  public void setTargetId(Long targetId) {
    this.targetId = targetId;
  }

  public TargetOutcome outcomeIds(List<Long> outcomeIds) {
    this.outcomeIds = outcomeIds;
    return this;
  }

  public TargetOutcome addOutcomeIdsItem(Long outcomeIdsItem) {
    this.outcomeIds.add(outcomeIdsItem);
    return this;
  }

  /**
   * The list of outcome cohort ids
   * @return outcomeIds
   **/
  @JsonProperty("outcomeIds")
  @NotNull
  public List<Long> getOutcomeIds() {
    return outcomeIds;
  }

  public void setOutcomeIds(List<Long> outcomeIds) {
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
    TargetOutcome targetOutcome = (TargetOutcome) o;
    return Objects.equals(this.targetId, targetOutcome.targetId) &&
        Objects.equals(this.outcomeIds, targetOutcome.outcomeIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(targetId, outcomeIds);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TargetOutcome {\n");
    
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
