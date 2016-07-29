/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.cohortdefinition;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author cknoll1
 */

@JsonIgnoreProperties(ignoreUnknown=true)
public class CohortExpression {
  
  @JsonProperty("Title")  
  public String title;
  
  @JsonProperty("PrimaryCriteria")
  public PrimaryCriteria primaryCriteria;

  @JsonProperty("AdditionalCriteria")
  public CriteriaGroup additionalCriteria;
  
  @JsonProperty("ConceptSets")
  public ConceptSet[] conceptSets;
  
  @JsonProperty("QualifiedLimit")  
  public ResultLimit qualifiedLimit = new ResultLimit();
  
  @JsonProperty("ExpressionLimit")
  public ResultLimit expressionLimit = new ResultLimit();

  @JsonProperty("InclusionRules")
  public List<InclusionRule> inclusionRules = new ArrayList<>();
  
  @JsonProperty("EndStrategy")
  public EndStrategy endStrategy;
  
}
