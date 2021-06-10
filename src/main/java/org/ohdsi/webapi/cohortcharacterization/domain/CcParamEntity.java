package org.ohdsi.webapi.cohortcharacterization.domain;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.ohdsi.analysis.WithId;
import org.ohdsi.analysis.cohortcharacterization.design.CohortCharacterization;
import org.ohdsi.analysis.cohortcharacterization.design.CohortCharacterizationParam;

@Entity
@Table(name = "cc_param")
public class CcParamEntity implements CohortCharacterizationParam, WithId<Long> {
    
    @Id
    @GenericGenerator(
        name = "cc_param_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @Parameter(name = "sequence_name", value = "cc_param_sequence"),
            @Parameter(name = "increment_size", value = "1")
        }
    )
    @GeneratedValue(generator = "cc_param_generator")
    private Long id;
    @ManyToOne(optional = false, targetEntity = CohortCharacterizationEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "cohort_characterization_id")
    private CohortCharacterization cohortCharacterization;
    @Column
    private String name;
    @Column
    private String value;
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public CohortCharacterization getCohortCharacterization() {
        return cohortCharacterization;
    }

    public void setCohortCharacterization(final CohortCharacterization cohortCharacterization) {
        this.cohortCharacterization = cohortCharacterization;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof CcParamEntity)) return false;
        final CcParamEntity that = (CcParamEntity) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), super.hashCode());
    }
}
