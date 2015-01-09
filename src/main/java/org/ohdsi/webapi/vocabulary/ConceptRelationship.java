package org.ohdsi.webapi.vocabulary;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author fdefalco
 */
@JsonInclude(Include.NON_NULL)
public class ConceptRelationship {

    @JsonProperty("RELATIONSHIP_NAME")    
    public String relationshipName;
    @JsonProperty("RELATIONSHIP_DISTANCE")    
    public int relationshipDistance;
}
