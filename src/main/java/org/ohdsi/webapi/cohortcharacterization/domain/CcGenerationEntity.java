package org.ohdsi.webapi.cohortcharacterization.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.ohdsi.webapi.common.generation.CommonGeneration;

@Entity
@Table(name = "cc_generation")
public class CcGenerationEntity extends CommonGeneration {

    @ManyToOne(targetEntity = CohortCharacterizationEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "cc_id")
    private CohortCharacterizationEntity cohortCharacterization;

    public CohortCharacterizationEntity getCohortCharacterization() {
        return cohortCharacterization;
    }
}
