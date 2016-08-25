/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.cohortdefinition;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author cknoll1
 */
public class PrimaryCriteria {
  
  @JsonProperty("CriteriaList")  
  public Criteria[] criteriaList = new Criteria[0];
  
  @JsonProperty("ObservationWindow")  
  public ObservationFilter observationWindow;
  
  @JsonProperty("PrimaryCriteriaLimit")  
  public ResultLimit primaryLimit = new ResultLimit();

}
