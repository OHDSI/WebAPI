/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.cohortresults;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 *
 * @author asena5
 */
public class ExposureCohortResult {        
    @JsonProperty("exposure_cohort_definition_id")
    public String exposureCohortDefinitionId;
    
    @JsonProperty("outcome_cohort_definition_id")
    public String outcomeCohortDefinitionId;
    
    @JsonProperty("num_persons_exposed")
    public long numPersonsExposed;

    @JsonProperty("num_persons_w_outcome_pre_exposure")
    public long numPersonsWithOutcomePreExposure;
    
    @JsonProperty("num_persons_w_outcome_post_exposure")
    public long numPersonsWithOutcomePostExposure;
    
    @JsonProperty("time_at_risk")
    public Float timeAtRisk;
    
    @JsonProperty("incidence_rate_1000py")
    public Float incidenceRate1000py;
}
