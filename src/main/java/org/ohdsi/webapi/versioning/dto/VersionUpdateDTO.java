package org.ohdsi.webapi.versioning.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.ohdsi.webapi.versioning.domain.VersionPK;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VersionUpdateDTO {
    @JsonProperty
    private Long assetId;

    @JsonProperty
    private int version;

    @JsonProperty
    private String comment;

    @JsonProperty
    private boolean archived;

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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public VersionPK getVersionPk() {
        return new VersionPK(assetId, version);
    }
}
