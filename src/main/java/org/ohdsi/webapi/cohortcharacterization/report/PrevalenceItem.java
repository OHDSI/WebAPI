package org.ohdsi.webapi.cohortcharacterization.report;

import org.ohdsi.webapi.cohortcharacterization.dto.CcPrevalenceStat;

import java.util.ArrayList;
import java.util.List;

public class PrevalenceItem<T extends PrevalenceItem> extends ExportItem<T> {
    protected final double MAX_DIFF = 1000.0d; // we need to ensure a JSON-parsable value
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

    
    /**
     * Calculate Standardized Mean Difference of dichotomous (binary) variable 
     * From https://www.ncbi.nlm.nih.gov/pmc/articles/PMC3472075/pdf/sim0028-3083.pdf
     *
    **/
    @Override
    protected double calcDiff(PrevalenceItem another) {
      double pTarget = avg == null ? 0 : avg;
      double pCompare = another.avg == null ? 0 : another.avg;

      double pooledError = Math.sqrt(((pTarget * (1.0 - pTarget)) + (pCompare * (1.0 - pCompare)))/2);
      if (pooledError == 0) { 
        // undefined case where denom = 0
        if (pTarget != pCompare) {
          // return +/- INF based on if T is bigger.
          return pTarget > pCompare ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        } else {
          // pTreatment and pCompare are same, so return 0
          return 0.0d;
        }
      } else {
        // calculate the standard mean differnce
        return (pTarget - pCompare) / pooledError;
      }
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