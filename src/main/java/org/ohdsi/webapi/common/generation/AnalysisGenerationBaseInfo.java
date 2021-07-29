package org.ohdsi.webapi.common.generation;

import org.ohdsi.webapi.shiro.Entities.UserEntity;

import javax.persistence.*;

@Embeddable
@MappedSuperclass
public class AnalysisGenerationBaseInfo {

    @Column(name = "hash_code")
    protected Integer hashCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", updatable = false)
    protected UserEntity createdBy;

    public Integer getHashCode() {

        return hashCode;
    }

    public UserEntity getCreatedBy() {

        return createdBy;
    }
}
