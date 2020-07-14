package org.ohdsi.webapi.report;

import java.util.List;
import java.util.Objects;

public class CDMDashboard {

    private List<CDMAttribute> summary;
    private List<ConceptCountRecord> gender;
    private List<ConceptDistributionRecord> ageAtFirstObservation;
    private List<CumulativeObservationRecord> cumulativeObservation;
    private List<MonthObservationRecord> observedByMonth;

    public List<CDMAttribute> getSummary() {
        return summary;
    }

    public void setSummary(List<CDMAttribute> summary) {
        this.summary = summary;
    }

    /**
     * @return the gender
     */
    public List<ConceptCountRecord> getGender() {
        return gender;
    }

    /**
     * @param gender the gender to set
     */
    public void setGender(List<ConceptCountRecord> gender) {
        this.gender = gender;
    }

    /**
     * @return the ageAtFirstObservation
     */
    public List<ConceptDistributionRecord> getAgeAtFirstObservation() {
        return ageAtFirstObservation;
    }

    /**
     * @param ageAtFirstObservation the ageAtFirstObservation to set
     */
    public void setAgeAtFirstObservation(
            List<ConceptDistributionRecord> ageAtFirstObservation) {
        this.ageAtFirstObservation = ageAtFirstObservation;
    }

    /**
     * @return the cumulativeObservation
     */
    public List<CumulativeObservationRecord> getCumulativeObservation() {
        return cumulativeObservation;
    }

    /**
     * @param cumulativeObservation the cumulativeObservation to set
     */
    public void setCumulativeObservation(
            List<CumulativeObservationRecord> cumulativeObservation) {
        this.cumulativeObservation = cumulativeObservation;
    }

    /**
     * @return the observedByMonth
     */
    public List<MonthObservationRecord> getObservedByMonth() {
        return observedByMonth;
    }

    /**
     * @param observedByMonth the observedByMonth to set
     */
    public void setObservedByMonth(List<MonthObservationRecord> observedByMonth) {
        this.observedByMonth = observedByMonth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CDMDashboard that = (CDMDashboard) o;
        return Objects.equals(summary, that.summary) &&
                Objects.equals(gender, that.gender) &&
                Objects.equals(ageAtFirstObservation, that.ageAtFirstObservation) &&
                Objects.equals(cumulativeObservation, that.cumulativeObservation) &&
                Objects.equals(observedByMonth, that.observedByMonth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(summary, gender, ageAtFirstObservation, cumulativeObservation, observedByMonth);
    }
}
