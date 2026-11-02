package org.ohdsi.webapi.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.ohdsi.webapi.tag.dto.TagDTO;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class CommonEntityExtDTO extends CommonEntityDTO{
  @JsonProperty
  private Set<TagDTO> tags;

  public Set<TagDTO> getTags() {
    return tags;
  }

  public void setTags(Set<TagDTO> tags) {
    this.tags = tags;
  }
}
