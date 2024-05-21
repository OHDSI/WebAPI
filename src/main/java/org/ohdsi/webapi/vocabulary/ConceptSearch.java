package org.ohdsi.webapi.vocabulary;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author fdefalco
 */
public class ConceptSearch {
    public ConceptSearch() {

    }
    
    @JsonProperty("QUERY")
    public String query;
    
    @JsonProperty("DOMAIN_ID")
    public String[] domainId;
    
    @JsonProperty("VOCABULARY_ID")
    public String[] vocabularyId;
    
    @JsonProperty("STANDARD_CONCEPT")
    public String standardConcept;
    
    @JsonProperty("INVALID_REASON")
    public String invalidReason;
    
    @JsonProperty("CONCEPT_CLASS_ID")
    public String[] conceptClassId;
    
    @JsonProperty("IS_LEXICAL")
    public boolean isLexical;

}
