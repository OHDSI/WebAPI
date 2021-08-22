package org.ohdsi.webapi.versioning.domain;

import org.ohdsi.webapi.shiro.Entities.UserEntity;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import java.util.Date;
import java.util.Objects;

@MappedSuperclass
public abstract class Version {
    @EmbeddedId
    private VersionPK pk;

    @Column(name = "comment")
    private String comment;

    @Column(name = "archived")
    private boolean archived;

    @Column(name = "asset_json")
    private String assetJson;

    @Column(name = "created_date", updatable = false)
    private Date createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", updatable = false)
    private UserEntity createdBy;

    public VersionPK getPk() {
        return pk;
    }

    public void setPk(VersionPK pk) {
        this.pk = pk;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String description) {
        this.comment = description;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public String getAssetJson() {
        return assetJson;
    }

    public void setAssetJson(String assetJson) {
        this.assetJson = assetJson;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public UserEntity getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserEntity createdBy) {
        this.createdBy = createdBy;
    }

    private VersionPK doGetPK() {
        if (Objects.isNull(getPk())) {
            setPk(new VersionPK());
        }
        return getPk();
    }

    public Long getAssetId() {
        return doGetPK().getAssetId();
    }

    public void setAssetId(long assetId) {
        doGetPK().setAssetId(assetId);
    }

    public int getVersion() {
        return doGetPK().getVersion();
    }

    public void setVersion(int version) {
        this.doGetPK().setVersion(version);
    }
}
