package org.ohdsi.webapi.tag.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class AssetTagPK implements Serializable {
    @Column(name = "asset_id")
    private int assetId;

    @Column(name = "tag_id")
    private int tagId;

    public AssetTagPK() {
    }

    public AssetTagPK(int assetId, int tagId) {
        this.assetId = assetId;
        this.tagId = tagId;
    }

    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }

    public int getTagId() {
        return tagId;
    }

    public void setTagId(int tagId) {
        this.tagId = tagId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetTagPK that = (AssetTagPK) o;
        return assetId == that.assetId && tagId == that.tagId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetId, tagId);
    }
}
