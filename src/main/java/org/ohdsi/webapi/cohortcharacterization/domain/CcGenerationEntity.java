package org.ohdsi.webapi.cohortcharacterization.domain;

import org.ohdsi.webapi.cohortcharacterization.converter.SerializedCcToCcConverter;
import org.ohdsi.webapi.common.generation.CommonGeneration;

import javax.persistence.*;

@Entity
@Table(name = "cc_generation")
public class CcGenerationEntity extends CommonGeneration {

    @ManyToOne(targetEntity = CohortCharacterizationEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "cc_id")
    private CohortCharacterizationEntity cohortCharacterization;

    public CohortCharacterizationEntity getCohortCharacterization() {
        return cohortCharacterization;
    }

    public CohortCharacterizationEntity getDesign() {
            return info != null ? new SerializedCcToCcConverter().convertToEntityAttribute(info.getDesign()) : null;
    }
}
