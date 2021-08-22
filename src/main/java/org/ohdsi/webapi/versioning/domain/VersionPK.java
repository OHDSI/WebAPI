package org.ohdsi.webapi.versioning.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class VersionPK implements Serializable {
    @Column(name = "asset_id")
    private long assetId;

    @Column(name = "version")
    private int version;

    public VersionPK() {
    }

    public VersionPK(long assetId, int version) {
        this.assetId = assetId;
        this.version = version;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(long assetId) {
        this.assetId = assetId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VersionPK that = (VersionPK) o;
        return assetId == that.assetId && version == that.version;
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetId, version);
    }
}
