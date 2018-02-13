package org.ohdsi.webapi.cohortresults;

public class ProfileSampleRecord {
    private long genderConceptId;
    private long personId;
    private int ageGroup;

    public long getGenderConceptId() {
        return genderConceptId;
    }

    public void setGenderConceptId(long genderConceptId) {
        this.genderConceptId = genderConceptId;
    }

    public long getPersonId() {
        return personId;
    }

    public void setPersonId(long personId) {
        this.personId = personId;
    }

    public int getAgeGroup() {
        return ageGroup;
    }

    public void setAgeGroup(int ageGroup) {
        this.ageGroup = ageGroup;
    }
}
