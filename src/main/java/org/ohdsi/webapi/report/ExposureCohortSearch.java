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
public class ExposureCohortSearch {

    public ExposureCohortSearch(){}
    
    @JsonProperty("EXPOSURE_COHORT_LIST")
    public String[] exposureCohortList;
    
    @JsonProperty("OUTCOME_COHORT_LIST")
    public String[] outcomeCohortList;
    
    @JsonProperty("MIN_CELL_COUNT")
    public int minCellCount = 0;
}
