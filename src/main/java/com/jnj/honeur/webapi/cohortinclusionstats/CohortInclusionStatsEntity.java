package com.jnj.honeur.webapi.cohortinclusionstats;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity(name = "CohortInclusionStatsEntity")
@Table(name = "cohort_inclusion_stats")
@IdClass(CohortInclusionStatsEntityId.class)
public class CohortInclusionStatsEntity implements Serializable{

    private static final long serialVersionUID = -7595365875490423613L;

    @Id
    @Column(name = "cohort_definition_id")
    private Long cohortDefinitionId;

    @Id
    @Column(name = "rule_sequence")
    private int ruleSequence;

    @Column(name = "person_count")
    private Long personCount;

    @Column(name = "gain_count")
    private Long gainCount;

    @Column(name = "person_total")
    private Long personTotal;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CohortInclusionStatsEntity that = (CohortInclusionStatsEntity) o;
        return ruleSequence == that.ruleSequence &&
                Objects.equals(cohortDefinitionId, that.cohortDefinitionId) &&
                Objects.equals(personCount, that.personCount) &&
                Objects.equals(gainCount, that.gainCount) &&
                Objects.equals(personTotal, that.personTotal);
    }

    @Override
    public int hashCode() {

        return Objects.hash(cohortDefinitionId, ruleSequence, personCount, gainCount, personTotal);
    }

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

    public Long getPersonCount() {
        return personCount;
    }

    public void setPersonCount(Long personCount) {
        this.personCount = personCount;
    }

    public Long getGainCount() {
        return gainCount;
    }

    public void setGainCount(Long gainCount) {
        this.gainCount = gainCount;
    }

    public Long getPersonTotal() {
        return personTotal;
    }

    public void setPersonTotal(Long personTotal) {
        this.personTotal = personTotal;
    }
}
