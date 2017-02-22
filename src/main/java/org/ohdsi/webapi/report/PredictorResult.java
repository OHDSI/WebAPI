/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.report;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author asena5
 */
public class PredictorResult {
    @JsonProperty("exposure_cohort_definition_id")
    public String exposureCohortDefinitionId;
    
    @JsonProperty("outcome_cohort_definition_id")
    public String outcomeCohortDefinitionId;
    
    @JsonProperty("concept_id")
    public String conceptId;

    @JsonProperty("concept_name")
    public String conceptName;
    
    @JsonProperty("domain_id")
    public String domainId;
    
    @JsonProperty("concept_w_outcome")
    public String conceptWithOutcome;    

    @JsonProperty("pct_outcome_w_concept")
    public String pctOutcomeWithConcept;    

    @JsonProperty("pct_nooutcome_w_concept")
    public String pctNoOutcomeWithConcept;    

    @JsonProperty("abs_std_diff")
    public String absStdDiff;    
}
