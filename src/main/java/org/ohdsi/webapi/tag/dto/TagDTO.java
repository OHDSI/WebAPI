package org.ohdsi.webapi.tag.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.ohdsi.webapi.service.dto.CommonEntityDTO;
import org.ohdsi.webapi.tag.domain.Tag;
import org.ohdsi.webapi.tag.domain.TagType;

import javax.persistence.Column;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TagDTO extends CommonEntityDTO {
    @JsonProperty
    int id;

    @JsonProperty
    private Set<TagDTO> groups;

    @JsonProperty
    private String name;

    @JsonProperty
    private TagType type;

    @JsonProperty
    private int count;

    @JsonProperty
    private Boolean showGroup;

    @JsonProperty
    private Boolean multiSelection;

    @JsonProperty
    private Boolean extra;

    @JsonProperty
    private String icon;

    @JsonProperty
    private String color;

    @JsonProperty
    private Boolean mandatory;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TagType getType() {
        return type;
    }

    public void setType(TagType type) {
        this.type = type;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Boolean getShowGroup() {
        return showGroup;
    }

    public void setShowGroup(Boolean showGroup) {
        this.showGroup = showGroup;
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

    public Boolean getMultiSelection() {
        return multiSelection;
    }

    public void setMultiSelection(Boolean multiSelection) {
        this.multiSelection = multiSelection;
    }

    public Boolean getExtra() {
        return extra;
    }

    public void setExtra(Boolean extra) {
        this.extra = extra;
    }

    public Boolean getMandatory() {
        return mandatory;
    }

    public void setMandatory(Boolean mandatory) {
        this.mandatory = mandatory;
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
