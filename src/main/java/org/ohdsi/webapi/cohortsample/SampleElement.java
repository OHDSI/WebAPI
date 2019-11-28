package org.ohdsi.webapi.cohortsample;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.ws.rs.Consumes;

@Entity(name = "CohortSampleElement")
public class SampleElement {
    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "cohort_sample_id")
    private int sampleId;

    private int rank;

    @Column(name = "person_id")
    private long personId;

    @Column(name = "gender_concept_id")
    private long genderConceptId;

    @Column(name = "age")
    private int age;

    public int getSampleId() {
        return sampleId;
    }

    public void setSampleId(int sampleId) {
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
