package org.ohdsi.webapi.cohortcharacterization.report;

import org.ohdsi.webapi.cohortcharacterization.dto.CcPrevalenceStat;

import java.util.ArrayList;
import java.util.List;

public class PrevalenceItem<T extends PrevalenceItem> extends ExportItem<T> {
    protected final Integer cohortId;
    protected final String cohortName;
    protected final Long count;
    protected final Double pct;
    protected final Double avg;

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
        values.add(String.valueOf(this.getAnalysisId()));
        values.add(this.getAnalysisName());
        values.add(String.valueOf(this.getStrataId()));
        values.add(this.getStrataName());
        values.add(String.valueOf(this.cohortId));
        values.add(this.cohortName);
        values.add(String.valueOf(this.getCovariateId()));
        values.add(this.getCovariateName());
        values.add(this.getCovariateShortName());
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
        int res = getAnalysisId().compareTo(that.getAnalysisId());
        if (res == 0) {
            getCovariateName().compareToIgnoreCase(that.getCovariateName());
        }
        if (res == 0) {
            res = cohortName.compareToIgnoreCase(that.cohortName);
        }
        return res;
    }

    public Long getCount() {
        return count;
    }

    public Double getPct() {
        return pct;
    }

    public Double getAvg() {
        return avg;
    }

    public Integer getCohortId() {
        return cohortId;
    }

    public String getCohortName() {
        return cohortName;
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