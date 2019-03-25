package org.ohdsi.webapi.cohortcharacterization.domain;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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
