package org.ohdsi.webapi.tag.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.ohdsi.webapi.service.dto.CommonEntityDTO;
import org.ohdsi.webapi.tag.TagType;

import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown=true)
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
}
