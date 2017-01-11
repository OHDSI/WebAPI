package org.ohdsi.webapi.report;

import java.util.List;

/**
 * Created by taa7016 on 11/29/2016.
 */
public class CDMDeath {
    private List<ConceptQuartileRecord> ageAtDeath;
    private List<ConceptCountRecord> deathByType;
    private List<ConceptDecileRecord> prevalenceByGenderAgeYear;
    private List<PrevalenceRecord> prevalenceByMonth;

    public void setDeathByType(List<ConceptCountRecord> deathByType) {
        this.deathByType = deathByType;
    }

    public List<ConceptCountRecord> getDeathByType() {
        return deathByType;
    }

    public List<ConceptDecileRecord> getPrevalenceByGenderAgeYear() {
        return prevalenceByGenderAgeYear;
    }

    public void setPrevalenceByGenderAgeYear(List<ConceptDecileRecord> prevalenceByGenderAgeYear) {
        this.prevalenceByGenderAgeYear = prevalenceByGenderAgeYear;
    }

    public void setAgeAtDeath(List<ConceptQuartileRecord> ageAtDeath) {
        this.ageAtDeath = ageAtDeath;
    }

    public List<ConceptQuartileRecord> getAgeAtDeath() {
        return ageAtDeath;
    }

    public List<PrevalenceRecord> getPrevalenceByMonth() {
        return prevalenceByMonth;
    }

    public void setPrevalenceByMonth(List<PrevalenceRecord> prevalenceByMonth) {
        this.prevalenceByMonth = prevalenceByMonth;
    }
}
