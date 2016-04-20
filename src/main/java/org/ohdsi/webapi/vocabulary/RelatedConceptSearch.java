/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.vocabulary;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author asena5
 */
public class RelatedConceptSearch {
    public RelatedConceptSearch() {

    }
    
    @JsonProperty("VOCABULARY_ID")
    public String[] vocabularyId;
    
    @JsonProperty("CONCEPT_CLASS_ID")
    public String[] conceptClassId;    
    
    @JsonProperty("CONCEPT_ID")
    public long[] conceptId;    
}
