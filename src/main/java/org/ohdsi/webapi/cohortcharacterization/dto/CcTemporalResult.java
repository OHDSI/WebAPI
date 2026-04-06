package org.ohdsi.webapi.cohortcharacterization.dto;

public class CcTemporalResult extends AbstractTemporalResult {
    private Integer timeId;
    private Integer startDay;
    private Integer endDay;

    public Integer getTimeId() {
        return timeId;
    }

    public void setTimeId(Integer timeId) {
        this.timeId = timeId;
    }

    public Integer getStartDay() {
        return startDay;
    }

    public void setStartDay(Integer startDay) {
        this.startDay = startDay;
    }

    public Integer getEndDay() {
        return endDay;
    }

    public void setEndDay(Integer endDay) {
        this.endDay = endDay;
    }
}
