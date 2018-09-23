package org.ohdsi.webapi.cohortcharacterization.domain;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.ohdsi.webapi.cohortcharacterization.converter.SerializedCcToCcConverter;
import org.ohdsi.webapi.common.generation.AnalysisGenerationInfo;
import org.ohdsi.webapi.common.generation.CommonGeneration;
import org.ohdsi.webapi.shiro.Entities.UserEntity;

@Entity
@Table(name = "cc_generation")
public class CcGenerationEntity extends CommonGeneration {

    @ManyToOne(targetEntity = CohortCharacterizationEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "cc_id")
    private CohortCharacterizationEntity cohortCharacterization;

    @Embedded
    private AnalysisGenerationInfo info;

    public CohortCharacterizationEntity getCohortCharacterization() {
        return cohortCharacterization;
    }

    public CohortCharacterizationEntity getDesign() {
            return info != null ? new SerializedCcToCcConverter().convertToEntityAttribute(info.getDesign()) : null;
    }

    public Integer getHashCode() {
        return this.getDesign() != null ? this.getDesign().hashCode() : null;
    }

    public UserEntity getCreatedBy() {

        return this.info.getCreatedBy();
    }
}
