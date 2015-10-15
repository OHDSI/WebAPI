/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.evidence;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author asena5
 */
public class EvidenceSearch {
    public EvidenceSearch() {
    }
    
    @JsonProperty("CONDITION_CONCEPT_LIST")
    public String[] conditionConceptList;        

    @JsonProperty("INGREDIENT_CONCEPT_LIST")
    public String[] ingredientConceptList;
    
    @JsonProperty("EVIDENCE_TYPE_LIST")
    public String[] evidenceTypeList;
 }
