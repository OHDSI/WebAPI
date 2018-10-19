package org.ohdsi.webapi.estimation.specification;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class ComparativeCohortAnalysis {
  @JsonProperty("targetComparatorOutcomes")
  private List<TargetComparatorOutcomes> targetComparatorOutcomes = null;

  @JsonProperty("cohortMethodAnalysisList")
  private List<CohortMethodAnalysis> cohortMethodAnalysisList = null;

  public ComparativeCohortAnalysis targetComparatorOutcomes(List<TargetComparatorOutcomes> targetComparatorOutcomes) {
    this.targetComparatorOutcomes = targetComparatorOutcomes;
    return this;
  }

  public ComparativeCohortAnalysis addTargetComparatorOutcomesItem(TargetComparatorOutcomes targetComparatorOutcomesItem) {
    if (this.targetComparatorOutcomes == null) {
      this.targetComparatorOutcomes = new ArrayList<TargetComparatorOutcomes>();
    }
    this.targetComparatorOutcomes.add(targetComparatorOutcomesItem);
    return this;
  }

  /**
   * The list of targetComparatorOutcomes 
   * @return targetComparatorOutcomes
   **/
  @JsonProperty("targetComparatorOutcomes")
  public List<TargetComparatorOutcomes> getTargetComparatorOutcomes() {
    return targetComparatorOutcomes;
  }

  public void setTargetComparatorOutcomes(List<TargetComparatorOutcomes> targetComparatorOutcomes) {
    this.targetComparatorOutcomes = targetComparatorOutcomes;
  }

  public ComparativeCohortAnalysis cohortMethodAnalysisList(List<CohortMethodAnalysis> cohortMethodAnalysisList) {
    this.cohortMethodAnalysisList = cohortMethodAnalysisList;
    return this;
  }

  public ComparativeCohortAnalysis addCohortMethodAnalysisListItem(CohortMethodAnalysis cohortMethodAnalysisListItem) {
    if (this.cohortMethodAnalysisList == null) {
      this.cohortMethodAnalysisList = new ArrayList<CohortMethodAnalysis>();
    }
    this.cohortMethodAnalysisList.add(cohortMethodAnalysisListItem);
    return this;
  }

  /**
   * The list of comparative cohort analyses for CohortMethod 
   * @return cohortMethodAnalysisList
   **/
  @JsonProperty("cohortMethodAnalysisList")
  public List<CohortMethodAnalysis> getCohortMethodAnalysisList() {
    return cohortMethodAnalysisList;
  }

  public void setCohortMethodAnalysisList(List<CohortMethodAnalysis> cohortMethodAnalysisList) {
    this.cohortMethodAnalysisList = cohortMethodAnalysisList;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ComparativeCohortAnalysis comparativeCohortAnalysis = (ComparativeCohortAnalysis) o;
    return Objects.equals(this.targetComparatorOutcomes, comparativeCohortAnalysis.targetComparatorOutcomes) &&
        Objects.equals(this.cohortMethodAnalysisList, comparativeCohortAnalysis.cohortMethodAnalysisList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(targetComparatorOutcomes, cohortMethodAnalysisList);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ComparativeCohortAnalysis {\n");
    
    sb.append("    targetComparatorOutcomes: ").append(toIndentedString(targetComparatorOutcomes)).append("\n");
    sb.append("    cohortMethodAnalysisList: ").append(toIndentedString(cohortMethodAnalysisList)).append("\n");
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
