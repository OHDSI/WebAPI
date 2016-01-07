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
public class CriteriaGroup implements ICohortExpressionElement {

  @JsonProperty("Type")
  public String type;

  @JsonProperty("Count")
  public Integer count;
  
  @JsonProperty("CriteriaList")
  public AdditionalCriteria[] criteriaList = new AdditionalCriteria[0];

  @JsonProperty("Groups")
  public CriteriaGroup[] groups = new CriteriaGroup[0];

  @Override
  public String accept(ICohortExpressionElementVisitor visitor) {
    return visitor.visit(this);
  }
}

