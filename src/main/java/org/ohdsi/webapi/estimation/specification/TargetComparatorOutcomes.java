package org.ohdsi.webapi.estimation.specification;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;


public class TargetComparatorOutcomes extends TargetOutcome {
  @JsonProperty("comparatorId")
  private Long comparatorId = null;

  @JsonProperty("excludedCovariateConceptIds")
  private List<Long> excludedCovariateConceptIds = null;

  @JsonProperty("includedCovariateConceptIds")
  private List<Long> includedCovariateConceptIds = null;

  public TargetComparatorOutcomes comparatorId(Long comparatorId) {
    this.comparatorId = comparatorId;
    return this;
  }

  /**
   * Comparator cohort id
   * @return comparatorId
   **/
  @JsonProperty("comparatorId")
  @NotNull
  public Long getComparatorId() {
    return comparatorId;
  }

  public void setComparatorId(Long comparatorId) {
    this.comparatorId = comparatorId;
  }

  public TargetComparatorOutcomes excludedCovariateConceptIds(List<Long> excludedCovariateConceptIds) {
    this.excludedCovariateConceptIds = excludedCovariateConceptIds;
    return this;
  }

  public TargetComparatorOutcomes addExcludedCovariateConceptIdsItem(Long excludedCovariateConceptIdsItem) {
    if (this.excludedCovariateConceptIds == null) {
      this.excludedCovariateConceptIds = new ArrayList<Long>();
    }
    this.excludedCovariateConceptIds.add(excludedCovariateConceptIdsItem);
    return this;
  }

  /**
   * A list of concept IDs that cannot be used to construct covariates. This argument is to be used only for exclusionconcepts that are specific to the drug-comparator combination.
   * @return excludedCovariateConceptIds
   **/
  @JsonProperty("excludedCovariateConceptIds")
  public List<Long> getExcludedCovariateConceptIds() {
    return excludedCovariateConceptIds;
  }

  public void setExcludedCovariateConceptIds(List<Long> excludedCovariateConceptIds) {
    this.excludedCovariateConceptIds = excludedCovariateConceptIds;
  }

  public TargetComparatorOutcomes includedCovariateConceptIds(List<Long> includedCovariateConceptIds) {
    this.includedCovariateConceptIds = includedCovariateConceptIds;
    return this;
  }

  public TargetComparatorOutcomes addIncludedCovariateConceptIdsItem(Long includedCovariateConceptIdsItem) {
    if (this.includedCovariateConceptIds == null) {
      this.includedCovariateConceptIds = new ArrayList<Long>();
    }
    this.includedCovariateConceptIds.add(includedCovariateConceptIdsItem);
    return this;
  }

  /**
   * A list of concept IDs that must be used to construct covariates. This argument is to be used only for inclusion concepts that are specific to the drug-comparator combination.
   * @return includedCovariateConceptIds
   **/
  @JsonProperty("includedCovariateConceptIds")
  public List<Long> getIncludedCovariateConceptIds() {
    return includedCovariateConceptIds;
  }

  public void setIncludedCovariateConceptIds(List<Long> includedCovariateConceptIds) {
    this.includedCovariateConceptIds = includedCovariateConceptIds;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TargetComparatorOutcomes targetComparatorOutcomes = (TargetComparatorOutcomes) o;
    return Objects.equals(this.comparatorId, targetComparatorOutcomes.comparatorId) &&
        Objects.equals(this.excludedCovariateConceptIds, targetComparatorOutcomes.excludedCovariateConceptIds) &&
        Objects.equals(this.includedCovariateConceptIds, targetComparatorOutcomes.includedCovariateConceptIds) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(comparatorId, excludedCovariateConceptIds, includedCovariateConceptIds, super.hashCode());
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TargetComparatorOutcomes {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    comparatorId: ").append(toIndentedString(comparatorId)).append("\n");
    sb.append("    excludedCovariateConceptIds: ").append(toIndentedString(excludedCovariateConceptIds)).append("\n");
    sb.append("    includedCovariateConceptIds: ").append(toIndentedString(includedCovariateConceptIds)).append("\n");
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
