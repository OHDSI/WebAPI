package org.ohdsi.webapi.estimation.specification;

import java.util.ArrayList;
import java.util.List;
import org.ohdsi.analysis.estimation.comparativecohortanalysis.design.ComparativeCohortAnalysis;

public class ComparativeCohortAnalysisImpl implements ComparativeCohortAnalysis {
  private List<TargetComparatorOutcomesImpl> targetComparatorOutcomes = null;
  private List<CohortMethodAnalysisImpl> cohortMethodAnalysisList = null;

  public ComparativeCohortAnalysisImpl addTargetComparatorOutcomesItem(TargetComparatorOutcomesImpl targetComparatorOutcomesItem) {
    if (this.targetComparatorOutcomes == null) {
      this.targetComparatorOutcomes = new ArrayList<>();
    }
    this.targetComparatorOutcomes.add(targetComparatorOutcomesItem);
    return this;
  }

  /**
   * The list of targetComparatorOutcomes 
   * @return targetComparatorOutcomes
   **/
  @Override
  public List<TargetComparatorOutcomesImpl> getTargetComparatorOutcomes() {
    return targetComparatorOutcomes;
  }

  public void setTargetComparatorOutcomes(List<TargetComparatorOutcomesImpl> targetComparatorOutcomes) {
    this.targetComparatorOutcomes = targetComparatorOutcomes;
  }

  public ComparativeCohortAnalysisImpl addCohortMethodAnalysisListItem(CohortMethodAnalysisImpl cohortMethodAnalysisListItem) {
    if (this.cohortMethodAnalysisList == null) {
      this.cohortMethodAnalysisList = new ArrayList<>();
    }
    this.cohortMethodAnalysisList.add(cohortMethodAnalysisListItem);
    return this;
  }

  /**
   * The list of comparative cohort analyses for CohortMethod 
   * @return cohortMethodAnalysisList
   **/
  @Override
  public List<CohortMethodAnalysisImpl> getCohortMethodAnalysisList() {
    return cohortMethodAnalysisList;
  }

  public void setCohortMethodAnalysisList(List<CohortMethodAnalysisImpl> cohortMethodAnalysisList) {
    this.cohortMethodAnalysisList = cohortMethodAnalysisList;
  }
}
