package org.ohdsi.webapi.cohortcharacterization.dto;

import org.ohdsi.analysis.cohortcharacterization.result.PrevalenceStat;

public class CcPrevalenceStat extends CcResult implements PrevalenceStat {
    
    private String timeWindow;
    private Long value;
    private Double proportion;
    private Double avg;
    private Long covariateId;
    private String covariateName;
    private Long conceptId;
    private String conceptName;
    private Long count;
    private long distance = 0;
    
    @Override
    public Double getProportion() {
        return this.proportion;
    }

    @Override
    public Long getCovariateId() {
        return covariateId;
    }

    @Override
    public String getCovariateName() {
        return covariateName;
    }

    @Override
    public Long getConceptId() {
        return conceptId;
    }

    @Override
    public Long getCount() {
        return count;
    }

    public void setCovariateId(final Long covariateId) {
        this.covariateId = covariateId;
    }

    public void setCovariateName(final String covariateName) {
        this.covariateName = covariateName;
    }

    public void setConceptId(final Long conceptId) {
        this.conceptId = conceptId;
    }

    public void setCount(final Long count) {
        this.count = count;
    }
    
    public String getTimeWindow() {
        return timeWindow;
    }

    public void setTimeWindow(final String timeWindow) {
        this.timeWindow = timeWindow;
    }

    public void setProportion(Double proportion) {

        this.proportion = proportion;
    }

    public Double getAvg() {

        return avg;
    }

    public void setAvg(Double avg) {

        this.avg = avg;
    }

    public long getDistance() {
        return distance;
    }

    public void setDistance(long distance) {
        this.distance = distance;
    }

    public String getConceptName() {
        return conceptName;
    }

    public void setConceptName(String conceptName) {
        this.conceptName = conceptName;
    }
}
