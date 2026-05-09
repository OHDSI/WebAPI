package org.ohdsi.webapi.cohortcharacterization.dto;

import org.ohdsi.analysis.cohortcharacterization.result.DistributionStat;

public class CcDistributionStat extends CcPrevalenceStat implements DistributionStat {

    private Double avg;
    private Double stdDev;
    private Double min;
    private Double p10;
    private Double p25;
    private Double median;
    private Double p75;
    private Double p90;
    private Double max;
    private Integer aggregateId;
    private String aggregateName;
    private Boolean missingMeansZero;
    
    @Override
    public Double getAvg() {
        return avg;
    }

    @Override
    public Double getStdDev() {
        return stdDev;
    }

    @Override
    public Double getMin() {
        return min;
    }

    @Override
    public Double getP10() {
        return p10;
    }

    @Override
    public Double getP25() {
        return p25;
    }

    @Override
    public Double getMedian() {
        return median;
    }

    @Override
    public Double getP75() {
        return p75;
    }

    @Override
    public Double getP90() {
        return p90;
    }

    @Override
    public Double getMax() {
        return max;
    }

    public void setAvg(final Double avg) {

        this.avg = avg;
    }

    public void setStdDev(final Double stdDev) {

        this.stdDev = stdDev;
    }

    public void setMin(final Double min) {

        this.min = min;
    }

    public void setP10(final Double p10) {

        this.p10 = p10;
    }

    public void setP25(final Double p25) {

        this.p25 = p25;
    }

    public void setMedian(final Double median) {

        this.median = median;
    }

    public void setP75(final Double p75) {

        this.p75 = p75;
    }

    public void setP90(final Double p90) {

        this.p90 = p90;
    }

    public void setMax(final Double max) {

        this.max = max;
    }

    public Integer getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(Integer aggregateId) {
        this.aggregateId = aggregateId;
    }

    public String getAggregateName() {
        return aggregateName;
    }

    public void setAggregateName(String aggregateName) {
        this.aggregateName = aggregateName;
    }

    @Override
    public Boolean isMissingMeansZero() {
        return missingMeansZero;
    }

    public void setMissingMeansZero(Boolean missingMeansZero) {
        this.missingMeansZero = missingMeansZero;
    }
}
