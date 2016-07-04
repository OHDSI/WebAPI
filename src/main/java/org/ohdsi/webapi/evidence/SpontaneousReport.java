/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.evidence;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 *
 * @author asena5
 */
public class SpontaneousReport {
    public SpontaneousReport() {
    }
    
    @JsonProperty("CONDITION_CONCEPT_ID")
    public String conditionConceptId;        

    @JsonProperty("CONDITION_CONCEPT_NAME")
    public String conditionConceptName;
    
    @JsonProperty("INGREDIENT_CONCEPT_ID")
    public String ingredientConceptId;

    @JsonProperty("INGREDIENT_CONCEPT_NAME")
    public String ingredientConceptName;

    @JsonProperty("REPORT_COUNT")
    public Integer reportCount;	

    @JsonProperty("PRR")
    public BigDecimal prr;       
}
