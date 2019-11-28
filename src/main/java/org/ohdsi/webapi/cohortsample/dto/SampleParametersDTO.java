package org.ohdsi.webapi.cohortsample.dto;

public class SampleParametersDTO {
    private int size;
    private Integer ageMin;
    private Integer ageMax;
    private Integer genderConceptId;

    public Integer getAgeMin() {
        return ageMin;
    }

    public void setAgeMin(Integer ageMin) {
        this.ageMin = ageMin;
    }

    public Integer getAgeMax() {
        return ageMax;
    }

    public void setAgeMax(Integer ageMax) {
        this.ageMax = ageMax;
    }

    public Integer getGenderConceptId() {
        return genderConceptId;
    }

    public void setGenderConceptId(Integer genderConceptId) {
        this.genderConceptId = genderConceptId;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
