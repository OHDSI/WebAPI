package org.ohdsi.webapi.common.generation;

import org.ohdsi.webapi.shiro.Entities.UserEntity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class  AnalysisGenerationInfo {

    @Column(name = "design")
    protected String design;

    @Column(name = "hash_code")
    protected Integer hashCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", updatable = false)
    protected UserEntity createdBy;

    public String getDesign() {

        return design;
    }

    public Integer getHashCode() {

        return hashCode;
    }

    public UserEntity getCreatedBy() {

        return createdBy;
    }
}
