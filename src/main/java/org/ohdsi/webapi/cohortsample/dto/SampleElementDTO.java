package org.ohdsi.webapi.cohortsample.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SampleElementDTO {
    private Integer sampleId;

    private int rank;

    private long personId;

    private long genderConceptId;

    private int age;

    public Integer getSampleId() {
        return sampleId;
    }

    public void setSampleId(Integer sampleId) {
        this.sampleId = sampleId;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public long getPersonId() {
        return personId;
    }

    public void setPersonId(long personId) {
        this.personId = personId;
    }

    public long getGenderConceptId() {
        return genderConceptId;
    }

    public void setGenderConceptId(long genderConceptId) {
        this.genderConceptId = genderConceptId;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
