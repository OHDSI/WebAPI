package org.ohdsi.webapi.shiny.summary;

public class DataSourceSummary {
    private String sourceName;
    private String numberOfPersons;
    private String female;
    private String male;
    private String ageAtFirstObservation;
    private String cumulativeObservation;
    private String continuousObservationCoverage;

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public void setNumberOfPersons(String numberOfPersons) {
        this.numberOfPersons = numberOfPersons;
    }

    public void setFemale(String female) {
        this.female = female;
    }

    public void setMale(String male) {
        this.male = male;
    }

    public void setAgeAtFirstObservation(String ageAtFirstObservation) {
        this.ageAtFirstObservation = ageAtFirstObservation;
    }

    public void setCumulativeObservation(String cumulativeObservation) {
        this.cumulativeObservation = cumulativeObservation;
    }

    public void setContinuousObservationCoverage(String continuousObservationCoverage) {
        this.continuousObservationCoverage = continuousObservationCoverage;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getNumberOfPersons() {
        return numberOfPersons;
    }

    public String getFemale() {
        return female;
    }

    public String getMale() {
        return male;
    }

    public String getAgeAtFirstObservation() {
        return ageAtFirstObservation;
    }

    public String getCumulativeObservation() {
        return cumulativeObservation;
    }

    public String getContinuousObservationCoverage() {
        return continuousObservationCoverage;
    }
}
