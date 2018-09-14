package org.ohdsi.webapi.cohortcharacterization.domain;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.ohdsi.analysis.cohortcharacterization.design.CohortCharacterization;
import org.ohdsi.webapi.cohortcharacterization.converter.SerializedCcToCcConverter;
import org.ohdsi.webapi.common.CommonGeneration;
import org.ohdsi.webapi.source.Source;

@Entity
@Table(name = "cc_generation")
public class CcGenerationEntity extends CommonGeneration {

    @ManyToOne(targetEntity = CohortCharacterizationEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "cohort_characterization_id")
    private CohortCharacterizationEntity cohortCharacterization;

    @Column(name = "design", updatable= false)
    @Convert(converter = SerializedCcToCcConverter.class)
    private CohortCharacterization design;

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

    public CohortCharacterization getDesign() {

        return design;
    }
}
