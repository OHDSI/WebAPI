package org.ohdsi.webapi.shiny.posit.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ContentItemResponse extends ContentItem {
    private UUID guid;
    @JsonProperty("owner_guid")
    private UUID ownerGuid;
    private String id;

    public UUID getGuid() {
        return guid;
    }

    public void setGuid(UUID guid) {
        this.guid = guid;
    }

    public UUID getOwnerGuid() {
        return ownerGuid;
    }

    public void setOwnerGuid(UUID ownerGuid) {
        this.ownerGuid = ownerGuid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
