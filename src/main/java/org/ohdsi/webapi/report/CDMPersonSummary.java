package org.ohdsi.webapi.report;

import java.util.List;
import org.ohdsi.webapi.report.CohortAttribute;

/**
 * Created by taa7016 on 8/12/2016.
 */
public class CDMPersonSummary {

    private List<CohortAttribute> yearOfBirth;
    private List<CohortStatsRecord> yearOfBirthStats;

    private List<ConceptCountRecord> gender;
    private List<ConceptCountRecord> race;
    private List<ConceptCountRecord> ethnicity;

    /**
     * @return the YearOfBirth
     */
    public List<CohortAttribute> getYearOfBirth() {
        return yearOfBirth;
    }
    /**
     * @param yearOfBirth the YearOfBirth to set
     */
    public void setYearOfBirth(
            List<CohortAttribute> yearOfBirth) {
        this.yearOfBirth = yearOfBirth;
    }
    /**
     * @return the Summary
     */
    public List<CohortStatsRecord> getSummary() {
        return yearOfBirthStats;
    }
    /**
     * @param yearOfBirthStats the yearOfBirthStats to set
     */
    public void setSummary(List<CohortStatsRecord> yearOfBirthStats) {
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
