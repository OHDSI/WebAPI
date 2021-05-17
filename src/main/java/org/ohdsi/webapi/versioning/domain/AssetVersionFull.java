package org.ohdsi.webapi.versioning.domain;

import org.ohdsi.webapi.model.CommonEntity;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AssetVersionFull extends CommonEntity<Long> {
    @Column(name = "asset_id")
    private int assetId;

    @Column(name = "name")
    private String name;

    @Column(name = "version")
    private int version;

    @Column(name = "archived")
    private boolean archived;

    @Column(name = "asset_json")
    private String assetJson;

    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }

    public String getName() {
        return name;
    }

    public void setName(String description) {
        this.name = description;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
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

    public abstract Long getId();

    public abstract void setId(Long id);
}
