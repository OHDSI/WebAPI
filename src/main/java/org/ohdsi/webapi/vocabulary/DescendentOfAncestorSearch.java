/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.vocabulary;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author anthonygsena
 */
public class DescendentOfAncestorSearch {
    public DescendentOfAncestorSearch() {
    }
    
    @JsonProperty("CONCEPT_ID")
    public String conceptId;        

    @JsonProperty("ANCESTOR_VOCABULARY_ID")
    public String ancestorVocabularyId;
    
    @JsonProperty("ANCESTOR_CLASS_ID")
    public String ancestorClassId;

    @JsonProperty("SIBLING_VOCABULARY_ID")
    public String siblingVocabularyId;
    
    @JsonProperty("SIBLING_CLASS_ID")
    public String siblingClassId;
}
