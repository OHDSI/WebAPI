package org.ohdsi.webapi.cohortcharacterization.report;

import org.ohdsi.webapi.cohortdefinition.CohortDefinition;

import java.util.ArrayList;
import java.util.List;

public class ComparativeItem extends ExportItem<ComparativeItem> {
    public final boolean hasFirstItem;
    public final boolean hasSecondItem;
    public final Integer targetCohortId;
    public final String targetCohortName;
    public final Long targetCount;
    public final Double targetPct;
    public final Integer comparatorCohortId;
    public final String comparatorCohortName;
    public final Long comparatorCount;
    public final Double comparatorPct;
    public final double diff;

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

    private double calcDiff(ExportItem first, ExportItem second) {
        if (first != null && second != null) {
            return first.calcDiff(second);
        }
        return 0d;
    }

    @Override
    protected List<String> getValueList() {
        // Do not use parent function as this report has its own order of columns
        List<String> values = new ArrayList<>();
        values.add(String.valueOf(this.analysisId));
        values.add(this.analysisName);
        values.add(String.valueOf(this.strataId));
        values.add(this.strataName);
        values.add(String.valueOf(targetCohortId));
        values.add(targetCohortName);
        values.add(String.valueOf(comparatorCohortId));
        values.add(comparatorCohortName);
        values.add(String.valueOf(this.covariateId));
        values.add(this.covariateName);
        values.add(this.covariateShortName);
        values.add(this.targetCount != null ? String.valueOf(this.targetCount) : "0");
        values.add(this.targetPct != null ? String.valueOf(this.targetPct) : "0");
        values.add(this.comparatorCount != null ? String.valueOf(this.comparatorCount) : "0");
        values.add(this.comparatorPct != null ? String.valueOf(this.comparatorPct) : "0");
        values.add(String.format("%.4f", this.diff));
        return values;
    }

    @Override
    public int compareTo(ComparativeItem that) {
        int res = analysisId.compareTo(that.analysisId);
        if (res == 0) {
            covariateName.compareToIgnoreCase(that.covariateName);
        }
        return res;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ComparativeItem that = (ComparativeItem) o;

        if (targetCohortId != null ? !targetCohortId.equals(that.targetCohortId) : that.targetCohortId != null)
            return false;
        return comparatorCohortId != null ? comparatorCohortId.equals(that.comparatorCohortId) : that.comparatorCohortId == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (targetCohortId != null ? targetCohortId.hashCode() : 0);
        result = 31 * result + (comparatorCohortId != null ? comparatorCohortId.hashCode() : 0);
        return result;
    }
}