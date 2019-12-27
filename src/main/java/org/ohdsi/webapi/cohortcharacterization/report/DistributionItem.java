package org.ohdsi.webapi.cohortcharacterization.report;

import org.ohdsi.webapi.cohortcharacterization.dto.CcDistributionStat;

import java.util.ArrayList;
import java.util.List;

public class DistributionItem extends PrevalenceItem<DistributionItem> {
    private final Double stdDev;
    private final Double min;
    private final Double p10;
    private final Double p25;
    private final Double median;
    private final Double p75;
    private final Double p90;
    private final Double max;

    public DistributionItem(CcDistributionStat distributionStat, String cohortName) {
        super(distributionStat, cohortName);
        this.stdDev = distributionStat.getStdDev();
        this.min = distributionStat.getMin();
        this.p10 = distributionStat.getP10();
        this.p25 = distributionStat.getP25();
        this.median = distributionStat.getMedian();
        this.p75 = distributionStat.getP75();
        this.p90 = distributionStat.getP90();
        this.max = distributionStat.getMax();
    }

    @Override
    protected List<String> getValueList() {
        // Do not use parent function as this report has its own order of columns
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
        values.add(String.valueOf(this.avg));
        values.add(String.valueOf(this.stdDev));
        values.add(String.valueOf(this.min));
        values.add(String.valueOf(this.p10));
        values.add(String.valueOf(this.p25));
        values.add(String.valueOf(this.median));
        values.add(String.valueOf(this.p75));
        values.add(String.valueOf(this.p90));
        values.add(String.valueOf(this.max));
        return values;
    }

    @Override
    protected double calcDiff(DistributionItem another) {
        if (stdDev == null || another.stdDev == null || avg == null || another.avg == null) {
            return 0d;
        }
        double sd1 = stdDev;
        double sd2 = another.stdDev;

        double sd = Math.sqrt(sd1 * sd1 + sd2 * sd2);
        // prevent division by zero
        return sd != 0D ? (avg - another.avg) / sd : 0;
    }

    public Double getStdDev() {
        return stdDev;
    }

    public Double getMin() {
        return min;
    }

    public Double getP10() {
        return p10;
    }

    public Double getP25() {
        return p25;
    }

    public Double getMedian() {
        return median;
    }

    public Double getP75() {
        return p75;
    }

    public Double getP90() {
        return p90;
    }

    public Double getMax() {
        return max;
    }
}
