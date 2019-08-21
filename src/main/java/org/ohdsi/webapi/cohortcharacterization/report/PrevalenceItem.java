package org.ohdsi.webapi.cohortcharacterization.report;

import org.ohdsi.webapi.cohortcharacterization.dto.CcPrevalenceStat;

import java.util.ArrayList;
import java.util.List;

public class PrevalenceItem<T extends PrevalenceItem> extends ExportItem<T> {
    public final Integer cohortId;
    public final String cohortName;
    public final Long count;
    public final Double pct;
    public final Double avg;

    public PrevalenceItem(CcPrevalenceStat prevalenceStat, String cohortName) {
        super(prevalenceStat);
        this.cohortId = prevalenceStat.getCohortId();
        this.cohortName = cohortName;
        this.count = prevalenceStat.getCount();
        this.avg = prevalenceStat.getAvg();
        this.pct = prevalenceStat.getAvg() * 100;
    }

    @Override
    protected List<String> getValueList() {
        List<String> values = new ArrayList<>();
        values.add(String.valueOf(this.analysisId));
        values.add(this.analysisName);
        values.add(String.valueOf(this.strataId));
        values.add(this.strataName);
        values.add(String.valueOf(this.cohortId));
        values.add(this.cohortName);
        values.add(String.valueOf(this.covariateId));
        values.add(this.covariateName);
        values.add(this.covariateShortName);
        values.add(String.valueOf(this.count));
        values.add(String.valueOf(this.pct));
        return values;
    }

    @Override
    protected double calcDiff(PrevalenceItem another) {
        if (count == null || another.count == null || avg == null || another.avg == null) {
            return 0d;
        }
        double n1 = count / avg;
        double n2 = another.count / another.avg;

        double mean1 = count / n1;
        double mean2 = another.count / n2;

        double sd1 = Math.sqrt((n1 * count + count) / (n1 * n1));
        double sd2 = Math.sqrt((n2 * another.count + another.count) / (n2 * n2));

        double sd = Math.sqrt(sd1 * sd1 + sd2 * sd2);

        return (mean2 - mean1) / sd;
    }

    @Override
    public int compareTo(PrevalenceItem that) {
        int res = analysisId.compareTo(that.analysisId);
        if (res == 0) {
            covariateName.compareToIgnoreCase(that.covariateName);
        }
        if (res == 0) {
            res = cohortName.compareToIgnoreCase(that.cohortName);
        }
        return res;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        PrevalenceItem<?> that = (PrevalenceItem<?>) o;

        return cohortId != null ? cohortId.equals(that.cohortId) : that.cohortId == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (cohortId != null ? cohortId.hashCode() : 0);
        return result;
    }
}