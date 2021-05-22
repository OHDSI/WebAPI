package org.ohdsi.webapi.versioning.domain;


import org.ohdsi.webapi.shiro.Entities.UserEntity;

import java.util.Date;

// Projection class
public class VersionBase {
    private Long assetId;

    private String comment;

    private int version;

    private UserEntity createdBy;

    private Date createdDate;

    private boolean archived;

    public VersionBase(long assetId, String comment, int version, UserEntity createdBy, Date createdDate, boolean archived) {
        this.assetId = assetId;
        this.comment = comment;
        this.version = version;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.archived = archived;
    }

    public Long getAssetId() {
        return assetId;
    }

    public String getComment() {
        return comment;
    }

    public int getVersion() {
        return version;
    }

    public UserEntity getCreatedBy() {
        return createdBy;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public boolean isArchived() {
        return archived;
    }
}
