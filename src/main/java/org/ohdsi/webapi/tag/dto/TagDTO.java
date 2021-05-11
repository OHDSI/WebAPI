package org.ohdsi.webapi.tag.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.ohdsi.webapi.service.dto.CommonEntityDTO;
import org.ohdsi.webapi.tag.domain.TagType;

import java.util.Objects;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TagDTO extends CommonEntityDTO {
    @JsonProperty
    Integer id;

    @JsonProperty
    private Set<TagDTO> groups;

    @JsonProperty
    private String name;

    @JsonProperty
    private TagType type;

    @JsonProperty
    private int count;

    @JsonProperty
    private boolean showGroup;

    @JsonProperty
    private boolean multiSelection;

    @JsonProperty
    private boolean permissionProtected;

    @JsonProperty
    private String icon;

    @JsonProperty
    private String color;

    @JsonProperty
    private boolean mandatory;

    @JsonProperty
    private boolean allowCustom;

    @JsonProperty
    private String description;

    @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Set<TagDTO> getGroups() {
        return groups;
    }

    public void setGroups(Set<TagDTO> groups) {
        this.groups = groups;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public TagType getType() {
        return type;
    }

    public void setType(TagType type) {
        this.type = type;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isShowGroup() {
        return showGroup;
    }

    public void setShowGroup(boolean showGroup) {
        this.showGroup = showGroup;
    }

    public boolean isMultiSelection() {
        return multiSelection;
    }

    public void setMultiSelection(boolean multiSelection) {
        this.multiSelection = multiSelection;
    }

    public boolean isPermissionProtected() {
        return permissionProtected;
    }

    public void setPermissionProtected(boolean permissionProtected) {
        this.permissionProtected = permissionProtected;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public boolean isAllowCustom() {
        return allowCustom;
    }

    public void setAllowCustom(boolean allowCustom) {
        this.allowCustom = allowCustom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagDTO tag = (TagDTO) o;
        return name.equals(tag.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
