package org.ohdsi.webapi.versioning.domain;


import org.ohdsi.webapi.shiro.Entities.UserEntity;

import java.util.Date;

public interface AssetVersionBase {
    Long getId();

    int getAssetId();

    String getComment();

    int getVersion();

    UserEntity getCreatedBy();

    Date getCreatedDate();

    boolean isArchived();
}
