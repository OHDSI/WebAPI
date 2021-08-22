package org.ohdsi.webapi.tag.domain;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity(name = "IrTag")
@Table(name = "ir_tag")
public class IrTag {
    @EmbeddedId
    private AssetTagPK assetId;

    @OneToOne(optional = false, targetEntity = Tag.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", insertable = false, updatable = false)
    private Tag tag;

    public AssetTagPK getAssetId() {
        return assetId;
    }

    public void setAssetId(AssetTagPK assetId) {
        this.assetId = assetId;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }
}

