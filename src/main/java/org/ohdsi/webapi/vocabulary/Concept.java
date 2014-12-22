package org.ohdsi.webapi.vocabulary;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author fdefalco
 */
public class Concept {
    @JsonProperty("CONCEPT_ID")
    public Long conceptId;
    
    @JsonProperty("CONCEPT_NAME")
    public String conceptName;
    
    @JsonProperty("DOMAIN_ID")
    public String domainId;
    
    @JsonProperty("VOCABULARY_ID")
    public String vocabularyId;
    
    @JsonProperty("CONCEPT_CLASS_ID")
    public String conceptClassId;
}
