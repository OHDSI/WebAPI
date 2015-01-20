/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.cohortdefinition;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author cknoll1
 */

@JsonIgnoreProperties(ignoreUnknown=true)
public class CohortDefinition {
  
  @JsonProperty("Title")  
  public String title;
  
  @JsonProperty("PrimaryCriteria")
  public Criteria[] primaryCriteria;

  @JsonProperty("AdditionalCriteria")
  public CriteriaGroup additionalCriteria;
  
  @JsonProperty("Codesets")
  public Codeset[] codesets;
}
