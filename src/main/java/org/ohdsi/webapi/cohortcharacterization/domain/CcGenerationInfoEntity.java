package org.ohdsi.webapi.cohortcharacterization.domain;

import org.ohdsi.analysis.cohortcharacterization.design.CohortCharacterization;
import org.ohdsi.webapi.cohortcharacterization.converter.SerializedCcToCcConverter;
import org.ohdsi.webapi.shiro.Entities.UserEntity;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "cc_generation_info")
public class CcGenerationInfoEntity implements CcGenerationInfo {

    @Id
    @Column(name = "job_execution_id")
    private Long id;

    @Column(name = "design", updatable= false)
    @Convert(converter = SerializedCcToCcConverter.class)
    private CohortCharacterization design;

    @Column(name = "hash_code")
    private Integer hashCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", updatable = false)
    private UserEntity createdBy;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    @Override
    public CohortCharacterization getDesign() {

        return design;
    }

    public void setDesign(CohortCharacterization design) {

        this.design = design;
    }

    @Override
    public Integer getHashCode() {

        return hashCode;
    }

    public void setHashCode(Integer hashCode) {

        this.hashCode = hashCode;
    }

    @Override
    public UserEntity getCreatedBy() {

        return createdBy;
    }

    public void setCreatedBy(UserEntity createdBy) {

        this.createdBy = createdBy;
    }
}
