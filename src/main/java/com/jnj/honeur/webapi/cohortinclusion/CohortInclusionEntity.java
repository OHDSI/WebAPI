package com.jnj.honeur.webapi.cohortinclusion;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity(name = "CohortInclusionEntity")
@Table(name = "cohort_inclusion")
@IdClass(CohortInclusionEntityId.class)
public class CohortInclusionEntity implements Serializable {

    @Id
    @Column(name = "cohort_definition_id")
    private Long cohortDefinitionId;

    @Id
    @Column(name = "rule_sequence")
    private int ruleSequence;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    public Long getCohortDefinitionId() {
        return cohortDefinitionId;
    }

    public void setCohortDefinitionId(Long cohortDefinitionId) {
        this.cohortDefinitionId = cohortDefinitionId;
    }

    public int getRuleSequence() {
        return ruleSequence;
    }

    public void setRuleSequence(int ruleSequence) {
        this.ruleSequence = ruleSequence;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {

        return Objects.hash(cohortDefinitionId, ruleSequence, name, description);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CohortInclusionEntity that = (CohortInclusionEntity) o;
        return ruleSequence == that.ruleSequence &&
                Objects.equals(cohortDefinitionId, that.cohortDefinitionId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(description, that.description);
    }
}
