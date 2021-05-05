package org.ohdsi.webapi.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.ohdsi.analysis.CohortMetadata;
import org.ohdsi.webapi.CommonDTO;
import org.ohdsi.webapi.cohortdefinition.CohortMetadataExt;
import org.ohdsi.webapi.tag.dto.TagDTO;
import org.ohdsi.webapi.user.dto.UserDTO;

import java.util.Date;
import java.util.List;
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
