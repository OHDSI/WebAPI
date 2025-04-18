package org.ohdsi.webapi.cohortcharacterization.report;

public class TemporalAnnualItem {
    private Long count;
    private Double avg;
    private Integer year;

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Double getAvg() {
        return avg;
    }

    public void setAvg(Double avg) {
        this.avg = avg;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}
