package org.ohdsi.webapi.model;

import org.ohdsi.analysis.WithId;
import org.ohdsi.webapi.shiro.Entities.UserEntity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

@MappedSuperclass
public abstract class CommonEntity<T extends Number> implements Serializable, WithId<T> {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", updatable = false)
    private UserEntity createdBy;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modified_by_id")
    private UserEntity modifiedBy;
    @Column(name = "created_date", updatable = false)
    private Date createdDate;
    @Column(name = "modified_date")
    private Date modifiedDate;

    public UserEntity getCreatedBy() {

        return createdBy;
    }

    public void setCreatedBy(UserEntity createdBy) {

        this.createdBy = createdBy;
    }

    public UserEntity getModifiedBy() {

        return modifiedBy;
    }

    public void setModifiedBy(UserEntity modifiedBy) {

        this.modifiedBy = modifiedBy;
    }

    public Date getCreatedDate() {

        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {

        this.createdDate = createdDate;
    }

    public Date getModifiedDate() {

        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {

        this.modifiedDate = modifiedDate;
    }
}
