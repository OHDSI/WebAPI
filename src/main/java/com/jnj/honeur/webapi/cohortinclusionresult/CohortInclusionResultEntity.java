package com.jnj.honeur.webapi.cohortinclusionresult;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity(name = "CohortInclusionResultEntity")
@Table(name = "cohort_inclusion_result")
@IdClass(CohortInclusionResultEntity.class)
public class CohortInclusionResultEntity implements Serializable {

    @Id
    @Column(name = "cohort_definition_id")
    private Long cohortDefinitionId;

    @Column(name = "inclusion_rule_mask")
    private Long inclusionRuleMask;

    @Column(name = "person_count")
    private Long personCount;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CohortInclusionResultEntity that = (CohortInclusionResultEntity) o;
        return Objects.equals(cohortDefinitionId, that.cohortDefinitionId) &&
                Objects.equals(inclusionRuleMask, that.inclusionRuleMask) &&
                Objects.equals(personCount, that.personCount);
    }

    @Override
    public int hashCode() {

        return Objects.hash(cohortDefinitionId, inclusionRuleMask, personCount);
    }

    public Long getCohortDefinitionId() {
        return cohortDefinitionId;
    }

    public void setCohortDefinitionId(Long cohortDefinitionId) {
        this.cohortDefinitionId = cohortDefinitionId;
    }

    public Long getInclusionRuleMask() {
        return inclusionRuleMask;
    }

    public void setInclusionRuleMask(Long inclusionRuleMask) {
        this.inclusionRuleMask = inclusionRuleMask;
    }

    public Long getPersonCount() {
        return personCount;
    }

    public void setPersonCount(Long personCount) {
        this.personCount = personCount;
    }
}
