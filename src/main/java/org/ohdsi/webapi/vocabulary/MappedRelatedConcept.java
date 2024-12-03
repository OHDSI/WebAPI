package org.ohdsi.webapi.vocabulary;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MappedRelatedConcept extends RelatedConcept {
    @JsonProperty("mapped_from")
    public Set<Long> mappedFromIds;
}
