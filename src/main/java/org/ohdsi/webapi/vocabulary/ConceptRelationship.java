package org.ohdsi.webapi.vocabulary;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author fdefalco
 */

public class ConceptRelationship {

    @JsonProperty("RELATIONSHIP_NAME")    
    public String relationshipName;
    @JsonProperty("RELATIONSHIP_DISTANCE")    
    public int relationshipDistance;
}
