package org.ohdsi.webapi.report;

import java.util.List;

/**
 * Created by taa7016 on 10/4/2016.
 */
public class CDMObservationPeriod {
    private List<ConceptDistributionRecord> ageAtFirst;
    private List<ConceptDistributionRecord> observationLength;
    private List<ConceptDistributionRecord> personsWithContinuousObservationsByYear;

    private List<CohortStatsRecord> personsWithContinuousObservationsByYearStats;
    private List<CohortStatsRecord> observationLengthStats;

    private List<ConceptQuartileRecord> ageByGender;
    private List<ConceptQuartileRecord> durationByGender;
    private List<ConceptQuartileRecord> durationByAgeDecile;

    private List<CumulativeObservationRecord> cumulativeObservation;

    private List<ConceptCountRecord> observationPeriodsPerPerson;

    private List<MonthObservationRecord> observedByMonth;

    /**
     * @return the ageAtFirst
     */
    public List<ConceptDistributionRecord> getAgeAtFirst() {
        return ageAtFirst;
    }
    /**
     * @param ageAtFirst the ageAtFirst to set
     */
    public void setAgeAtFirst(List<ConceptDistributionRecord> ageAtFirst) {
        this.ageAtFirst = ageAtFirst;
    }
    /**
     * @return the ageByGender
     */
    public List<ConceptQuartileRecord> getAgeByGender() {
        return ageByGender;
    }
    /**
     * @param ageByGender the ageByGender to set
     */
    public void setAgeByGender(List<ConceptQuartileRecord> ageByGender) {
        this.ageByGender = ageByGender;
    }
    /**
     * @return the observationLength
     */
    public List<ConceptDistributionRecord> getObservationLength() {
        return observationLength;
    }
    /**
     * @param observationLength the observationLength to set
     */
    public void setObservationLength(
            List<ConceptDistributionRecord> observationLength) {
        this.observationLength = observationLength;
    }
    /**
     * @return the durationByGender
     */
    public List<ConceptQuartileRecord> getDurationByGender() {
        return durationByGender;
    }
    /**
     * @param durationByGender the durationByGender to set
     */
    public void setDurationByGender(List<ConceptQuartileRecord> durationByGender) {
        this.durationByGender = durationByGender;
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
     * @return the durationByAgeDecile
     */
    public List<ConceptQuartileRecord> getDurationByAgeDecile() {
        return durationByAgeDecile;
    }
    /**
     * @param durationByAgeDecile the durationByAgeDecile to set
     */
    public void setDurationByAgeDecile(
            List<ConceptQuartileRecord> durationByAgeDecile) {
        this.durationByAgeDecile = durationByAgeDecile;
    }
    /**
     * @return the personsWithContinuousObservationsByYear
     */
    public List<ConceptDistributionRecord> getPersonsWithContinuousObservationsByYear() {
        return personsWithContinuousObservationsByYear;
    }
    /**
     * @param personsWithContinuousObservationsByYear the personsWithContinuousObservationsByYear to set
     */
    public void setPersonsWithContinuousObservationsByYear(
            List<ConceptDistributionRecord> personsWithContinuousObservationsByYear) {
        this.personsWithContinuousObservationsByYear = personsWithContinuousObservationsByYear;
    }
    /**
     * @return the observationPeriodsPerPerson
     */
    public List<ConceptCountRecord> getObservationPeriodsPerPerson() {
        return observationPeriodsPerPerson;
    }
    /**
     * @param observationPeriodsPerPerson the observationPeriodsPerPerson to set
     */
    public void setObservationPeriodsPerPerson(
            List<ConceptCountRecord> observationPeriodsPerPerson) {
        this.observationPeriodsPerPerson = observationPeriodsPerPerson;
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
    /**
     * @return the personsWithContinuousObservationsByYearStats
     */
    public List<CohortStatsRecord> getPersonsWithContinuousObservationsByYearStats() {
        return personsWithContinuousObservationsByYearStats;
    }
    /**
     * @param personsWithContinuousObservationsByYearStats the personsWithContinuousObservationsByYearStats to set
     */
    public void setPersonsWithContinuousObservationsByYearStats(
            List<CohortStatsRecord> personsWithContinuousObservationsByYearStats) {
        this.personsWithContinuousObservationsByYearStats = personsWithContinuousObservationsByYearStats;
    }
    /**
     * @return the observationLengthStats
     */
    public List<CohortStatsRecord> getObservationLengthStats() {
        return observationLengthStats;
    }
    /**
     * @param observationLengthStats the observationLengthStats to set
     */
    public void setObservationLengthStats(
            List<CohortStatsRecord> observationLengthStats) {
        this.observationLengthStats = observationLengthStats;
    }
}
