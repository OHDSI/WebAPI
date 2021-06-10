package org.ohdsi.webapi.vocabulary;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import org.ohdsi.circe.vocabulary.Concept;

/**
 *
 * @author fdefalco
 */
@JsonInclude(Include.NON_NULL)
public class RelatedConcept extends Concept {

    @JsonProperty("RELATIONSHIPS")
    public ArrayList<ConceptRelationship> relationships;

    public RelatedConcept() {
        relationships = new ArrayList<>();
    }

    @JsonProperty("RELATIONSHIP_CAPTION")
    public String GetRelationshipCaption() {
        String result = "";
        for (int i = 0; i < relationships.size(); i++) {
            if (i > 0)
                result += ", ";
            result += relationships.get(i).relationshipName;
        }
        return result;
    }
}
