package org.ohdsi.webapi.cohortcharacterization.dto;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.ohdsi.standardized_analysis_api.cohortcharacterization.design.CohortCharacterization;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.source.Source;
@Entity
@Table(name = "cc_generations")
public class CcGenerationEntity {
    @Id
    @Column
    private Long id;
    @ManyToOne(targetEntity = CohortCharacterizationEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "cohort_characterization_id")
    private CohortCharacterizationEntity cohortCharacterization;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private Source source;
    @Column(name = "date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
    @Column
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public CohortCharacterizationEntity getCohortCharacterization() {
        return cohortCharacterization;
    }

    public void setCohortCharacterization(final CohortCharacterizationEntity cohortCharacterization) {
        this.cohortCharacterization = cohortCharacterization;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(final Source source) {
        this.source = source;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(final Date date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }
}
