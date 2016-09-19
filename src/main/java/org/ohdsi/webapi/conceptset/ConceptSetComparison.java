/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.conceptset;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author Anthony Sena <https://github.com/ohdsi>
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConceptSetComparison {
    @JsonProperty("conceptId")
    public Long conceptId;    
    
    @JsonProperty("conceptIn1Only")
    public Long conceptIn1Only;

    @JsonProperty("conceptIn2Only")
    public Long conceptIn2Only;
    
    @JsonProperty("conceptIn1And2")
    public Long conceptIn1And2;
    
    @JsonProperty("conceptName")
    public String conceptName;
    
    @JsonProperty("standardConcept")
    public String standardConcept;
    
    @JsonProperty("invalidReason")
    public String invalidReason;
    
    @JsonProperty("conceptCode")
    public String conceptCode;
    
    @JsonProperty("domainId")
    public String domainId;
    
    @JsonProperty("vocabularyId")
    public String vocabularyId;
    
    @JsonProperty("conceptClassId")
    public String conceptClassId;
}
