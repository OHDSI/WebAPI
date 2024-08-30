package org.ohdsi.webapi.tool.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.ohdsi.webapi.shiro.Entities.UserEntity;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ToolDTO {
    private Integer id;
    private String name;
    private String url;
    private String description;
    private String createdByName;
    private String modifiedByName;
    private String createdDate;
    private String modifiedDate;
    private Boolean isEnabled;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public String getModifiedByName() {
        return modifiedByName;
    }

    public void setModifiedByName(String modifiedByName) {
        this.modifiedByName = modifiedByName;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(String modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public Boolean getEnabled() {
        return isEnabled;
    }

    public void setEnabled(Boolean enabled) {
        isEnabled = enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ToolDTO toolDTO = (ToolDTO) o;
        return Objects.equals(name, toolDTO.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
