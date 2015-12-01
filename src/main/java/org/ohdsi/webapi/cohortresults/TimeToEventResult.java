/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.cohortresults;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author asena5
 */
public class TimeToEventResult {
    @JsonProperty("exposure_cohort_definition_id")
    public String exposureCohortDefinitionId;
    
    @JsonProperty("outcome_cohort_definition_id")
    public String outcomeCohortDefinitionId;
    
    @JsonProperty("recordType")
    public String recordType;

    @JsonProperty("duration")
    public long duration;
    
    @JsonProperty("count_value")
    public long countValue;
    
    @JsonProperty("pctPersons")
    public double pctPersons;
}
