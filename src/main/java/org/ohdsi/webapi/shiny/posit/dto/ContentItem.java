package org.ohdsi.webapi.shiny.posit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ContentItem {
    private String name;
    private String title;
    private String description;
    @JsonProperty("access_type")
    private String accessType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAccessType() {
        return accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }
}

