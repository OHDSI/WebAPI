package org.ohdsi.webapi.cohortsample;

import java.util.List;

public class CohortSample {
    private Integer id;

    private String name;

    private int cohortDefinitionId;

    private Integer ageMin;

    private Integer ageMax;

    private Integer genderConceptId;

    private List<SampleElement> elements;

    private int size;

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getCohortDefinitionId() {
        return cohortDefinitionId;
    }

    public void setCohortDefinitionId(int cohortDefinitionId) {
        this.cohortDefinitionId = cohortDefinitionId;
    }

    public List<SampleElement> getElements() {
        return elements;
    }

    public void setElements(List<SampleElement> elements) {
        this.elements = elements;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Integer getAgeMin() {
        return ageMin;
    }

    public void setAgeMin(Integer ageMin) {
        if (ageMin == 0) {
            this.ageMin = null;
        } else {
            this.ageMin = ageMin;
        }
    }

    public Integer getAgeMax() {
        return ageMax;
    }

    public void setAgeMax(Integer ageMax) {
        if (ageMax == 0) {
            this.ageMax = null;
        } else {
            this.ageMax = ageMax;
        }
    }

    public Integer getGenderConceptId() {
        return genderConceptId;
    }

    public void setGenderConceptId(Integer genderConceptId) {
        if (genderConceptId == 0) {
            this.genderConceptId = null;
        } else {
            this.genderConceptId = genderConceptId;
        }
    }
}
