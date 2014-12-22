package org.ohdsi.webapi.vocabulary;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;

/**
 *
 * @author fdefalco
 */
public class RelatedConcept extends Concept {

    @JsonProperty("RELATIONSHIPS")       
    public ArrayList<ConceptRelationship> relationships;
    
    public RelatedConcept() {
        relationships = new ArrayList<>();
    }
}

