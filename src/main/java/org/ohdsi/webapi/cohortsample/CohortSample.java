package org.ohdsi.webapi.cohortsample;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.ohdsi.webapi.model.CommonEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import java.util.List;

@Entity(name = "CohortSample")
@Table(name = "cohort_sample")
public class CohortSample extends CommonEntity<Integer> {
    @Id
    @GenericGenerator(
            name = "cohort_sample_generator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "sequence_name", value = "cohort_sample_sequence"),
                    @Parameter(name = "increment_size", value = "1")
            }
    )
    @GeneratedValue(generator = "cohort_sample_generator")
    private Integer id;

    @Column
    private String name;

    @JoinColumn(name = "cohort_definition_id")
    private int cohortDefinitionId;

    @Column(name = "age_min")
    private Integer ageMin;

    @Column(name = "age_max")
    private Integer ageMax;

    @Column(name = "gender_concept_id")
    private Integer genderConceptId;

    @Column
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
