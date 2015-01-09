package org.ohdsi.webapi.vocabulary;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author fdefalco
 */
public class Vocabulary {
    @JsonProperty("VOCABULARY_ID")
    public String vocabularyId;
    
    @JsonProperty("VOCABULARY_NAME")
    public String vocabularyName;
    
    @JsonProperty("VOCABULARY_REFERENCE")
    public String vocabularyReference;
    
    @JsonProperty("VOCABULARY_VERSION")
    public String vocabularyVersion;
    
    @JsonProperty("VOCABULARY_CONCEPT_ID")
    public long vocabularyConceptId;
}
