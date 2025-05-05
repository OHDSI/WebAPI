package org.ohdsi.webapi.tag.domain;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity(name = "ConceptSetTag")
@Table(name = "concept_set_tag")
public class ConceptSetTag {
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

