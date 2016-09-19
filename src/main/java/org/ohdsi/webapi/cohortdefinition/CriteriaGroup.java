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
public class CriteriaGroup {

  @JsonProperty("Type")
  public String type;

  @JsonProperty("Count")
  public Integer count;
  
  @JsonProperty("CriteriaList")
  public CorelatedCriteria[] criteriaList = new CorelatedCriteria[0];
  
  @JsonProperty("DemographicCriteriaList")
  public DemographicCriteria[] demographicCriteriaList = new DemographicCriteria[0];  

  @JsonProperty("Groups")
  public CriteriaGroup[] groups = new CriteriaGroup[0];
}

