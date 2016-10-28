package org.ohdsi.webapi.report;

import java.util.List;

/**
 * Created by taa7016 on 8/12/2016.
 */
public class CDMPersonSummary {

    private List<CDMAttribute> summary;
    private List<ConceptCountRecord> gender;
    private List<ConceptCountRecord> race;
    private List<ConceptCountRecord> ethnicity;
    private List<ConceptDistributionRecord> yearOfBirth;
    private List<CohortStatsRecord> yearOfBirthStats;

    /**
     * @return the summary
     */
    public List<CDMAttribute> getSummary() {
        return summary;
    }

    /**
     * @param summary the summary to set
     */
    public void setSummary(List<CDMAttribute> summary) {
        this.summary = summary;
    }

    public List<ConceptDistributionRecord> getYearOfBirth() {
        return yearOfBirth;
    }

    public void setYearOfBirth(List<ConceptDistributionRecord> yearOfBirth) {
        this.yearOfBirth = yearOfBirth;
    }

    /**
     * @return the yearOfBirthStats
     */
    public List<CohortStatsRecord> getYearOfBirthStats() {
        return yearOfBirthStats;
    }

    /**
     * @param yearOfBirthStats the yearOfBirthStats to set
     */
    public void setYearOfBirthStats(List<CohortStatsRecord> yearOfBirthStats) {
        this.yearOfBirthStats = yearOfBirthStats;
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
     * @return the race
     */
    public List<ConceptCountRecord> getRace() {
        return race;
    }

    /**
     * @param race the race to set
     */
    public void setRace(List<ConceptCountRecord> race) {
        this.race = race;
    }

    /**
     * @return the ethnicity
     */
    public List<ConceptCountRecord> getEthnicity() {
        return ethnicity;
    }

    /**
     * @param ethnicity the ethnicity to set
     */
    public void setEthnicity(List<ConceptCountRecord> ethnicity) {
        this.ethnicity = ethnicity;
    }

}
