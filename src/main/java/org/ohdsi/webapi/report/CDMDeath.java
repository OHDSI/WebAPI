package org.ohdsi.webapi.report;

import java.util.List;

/**
 * Created by taa7016 on 11/29/2016.
 */
public class CDMDeath {
    private List<CDMAttribute> ageAtDeath;
    private List<CDMAttribute> deathByType;
    private List<CDMAttribute> prevalenceByGenderAgeYear;

    public List<CDMAttribute> getPrevalenceByMonth() {
        return prevalenceByMonth;
    }

    public void setPrevalenceByMonth(List<CDMAttribute> prevalenceByMonth) {
        this.prevalenceByMonth = prevalenceByMonth;
    }

    private List<CDMAttribute> prevalenceByMonth;

    public void setDeathByType(List<CDMAttribute> deathByType) {
        this.deathByType = deathByType;
    }

    public List<CDMAttribute> getDeathByType() {
        return deathByType;
    }

    public List<CDMAttribute> getPrevalenceByGenderAgeYear() {
        return prevalenceByGenderAgeYear;
    }

    public void setPrevalenceByGenderAgeYear(List<CDMAttribute> prevalenceByGenderAgeYear) {
        this.prevalenceByGenderAgeYear = prevalenceByGenderAgeYear;
    }

    public void setAgeAtDeath(List<CDMAttribute> ageAtDeath) {
        this.ageAtDeath = ageAtDeath;
    }

    public List<CDMAttribute> getAgeAtDeath() {
        return ageAtDeath;
    }
}
