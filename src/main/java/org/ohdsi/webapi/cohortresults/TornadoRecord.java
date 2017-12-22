package org.ohdsi.webapi.cohortresults;

public class TornadoRecord {
    private long genderConceptId;
    private int ageGroup;
    private long personCount;

    public long getGenderConceptId() {
        return genderConceptId;
    }

    public void setGenderConceptId(long genderConceptId) {
        this.genderConceptId = genderConceptId;
    }

    public int getAgeGroup() {
        return ageGroup;
    }

    public void setAgeGroup(int ageGroup) {
        this.ageGroup = ageGroup;
    }

    public long getPersonCount() {
        return personCount;
    }

    public void setPersonCount(long personCount) {
        this.personCount = personCount;
    }
}
