package org.ohdsi.webapi.shiny.posit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AddTagRequest {
    @JsonProperty("tag_id")
    private String tagId;

    public AddTagRequest(String tagId) {
        this.tagId = tagId;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }
}
