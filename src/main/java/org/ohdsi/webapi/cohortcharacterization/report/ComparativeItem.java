package org.ohdsi.webapi.cohortcharacterization.report;

import org.ohdsi.webapi.cohortdefinition.CohortDefinition;

import java.util.ArrayList;
import java.util.List;
import org.ohdsi.webapi.cohortcharacterization.dto.CcPrevalenceStat;

public class ComparativeItem extends ExportItem<ComparativeItem> {

  private final boolean hasFirstItem;
  private final boolean hasSecondItem;
  private final Integer targetCohortId;
  private final String targetCohortName;
  private final Long targetCount;
  private final Double targetPct;
  private final Integer comparatorCohortId;
  private final String comparatorCohortName;
  private final Long comparatorCount;
  private final Double comparatorPct;
  private final double diff;
  private static final CcPrevalenceStat EMPTY_ITEM;
  
  static {
    EMPTY_ITEM = new CcPrevalenceStat();
    EMPTY_ITEM.setAvg(0.0d);
    EMPTY_ITEM.setProportion(0.0d);
  }

  public ComparativeItem(PrevalenceItem firstItem, PrevalenceItem secondItem, CohortDefinition firstCohortDef,
          CohortDefinition secondCohortDef) {
    super(firstItem != null ? firstItem : secondItem);
    this.hasFirstItem = firstItem != null;
    this.hasSecondItem = secondItem != null;
    this.targetCohortId = firstCohortDef.getId();
    this.targetCohortName = firstCohortDef.getName();
    this.targetCount = firstItem != null ? firstItem.count : null;
    this.targetPct = firstItem != null ? firstItem.pct : null;
    this.comparatorCohortId = secondCohortDef.getId();
    this.comparatorCohortName = secondCohortDef.getName();
    this.comparatorCount = secondItem != null ? secondItem.count : null;
    this.comparatorPct = secondItem != null ? secondItem.pct : null;
    this.diff = calcDiff(firstItem, secondItem);
  }

  protected double calcDiff(ExportItem first, ExportItem second) {
    if (first == null) {
      first = new PrevalenceItem(EMPTY_ITEM, this.targetCohortName);
    }
    
    if (second == null) {
      second = new PrevalenceItem(EMPTY_ITEM, this.comparatorCohortName);
    }
    return first.calcDiff(second);
  }

  @Override
  protected List<String> getValueList() {
    // Do not use parent function as this report has its own order of columns
    List<String> values = new ArrayList<>();
    values.add(String.valueOf(this.getAnalysisId()));
    values.add(this.getAnalysisName());
    values.add(String.valueOf(this.getStrataId()));
    values.add(this.getStrataName());
    values.add(String.valueOf(targetCohortId));
    values.add(targetCohortName);
    values.add(String.valueOf(comparatorCohortId));
    values.add(comparatorCohortName);
    values.add(String.valueOf(this.getCovariateId()));
    values.add(this.getCovariateName());
    values.add(this.getCovariateShortName());
    values.add(this.targetCount != null ? String.valueOf(this.targetCount) : "0");
    values.add(this.targetPct != null ? String.valueOf(this.targetPct) : "0");
    values.add(this.comparatorCount != null ? String.valueOf(this.comparatorCount) : "0");
    values.add(this.comparatorPct != null ? String.valueOf(this.comparatorPct) : "0");
    values.add(String.format("%.4f", this.diff));
    return values;
  }

  @Override
  public int compareTo(ComparativeItem that) {
    int res = getAnalysisId().compareTo(that.getAnalysisId());
    if (res == 0) {
      getCovariateName().compareToIgnoreCase(that.getCovariateName());
    }
    return res;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    ComparativeItem that = (ComparativeItem) o;

    if (targetCohortId != null ? !targetCohortId.equals(that.targetCohortId) : that.targetCohortId != null) {
      return false;
    }
    return comparatorCohortId != null ? comparatorCohortId.equals(that.comparatorCohortId) : that.comparatorCohortId == null;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (targetCohortId != null ? targetCohortId.hashCode() : 0);
    result = 31 * result + (comparatorCohortId != null ? comparatorCohortId.hashCode() : 0);
    return result;
  }

  public boolean isHasFirstItem() {
    return hasFirstItem;
  }

  public boolean isHasSecondItem() {
    return hasSecondItem;
  }

  public Integer getTargetCohortId() {
    return targetCohortId;
  }

  public String getTargetCohortName() {
    return targetCohortName;
  }

  public Long getTargetCount() {
    return targetCount;
  }

  public Double getTargetPct() {
    return targetPct;
  }

  public Integer getComparatorCohortId() {
    return comparatorCohortId;
  }

  public String getComparatorCohortName() {
    return comparatorCohortName;
  }

  public Long getComparatorCount() {
    return comparatorCount;
  }

  public Double getComparatorPct() {
    return comparatorPct;
  }

  public double getDiff() {
    return diff;
  }
}
