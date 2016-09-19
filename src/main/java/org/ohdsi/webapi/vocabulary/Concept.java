package org.ohdsi.webapi.vocabulary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author fdefalco
 */
@JsonInclude(Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Concept {
    @JsonProperty("CONCEPT_ID")
    public Long conceptId;
    
    @JsonProperty("CONCEPT_NAME")
    public String conceptName;
    
    @JsonProperty("STANDARD_CONCEPT_CAPTION")
    public String GetStandardConcept() {
      if (standardConcept == null)
        return "Unknown";
      
      switch (standardConcept) {
        case "N" : 
          return "Non-Standard";
        case "S" :
          return "Standard";
        case "C" :
          return "Classification";
        default:
          return "Unknown";
      }
    }
    
    @JsonProperty("STANDARD_CONCEPT")
    public String standardConcept;
    
    @JsonProperty("INVALID_REASON_CAPTION")
    public String GetInvalidReason() {
      if (invalidReason == null)
        return "Unknown";
      
      switch (invalidReason) {
        case "V" : 
          return "Valid";
        case "D" :
          return "Invalid";
        case "U" :
            return "Invalid";
        default:
          return "Unknown";
      }
    }
    
    @JsonProperty("INVALID_REASON")
    public String invalidReason;
    
    @JsonProperty("CONCEPT_CODE")
    public String conceptCode;
    
    @JsonProperty("DOMAIN_ID")
    public String domainId;
    
    @JsonProperty("VOCABULARY_ID")
    public String vocabularyId;
    
    @JsonProperty("CONCEPT_CLASS_ID")
    public String conceptClassId;
}
