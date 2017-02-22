/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.cohortdefinition;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.ohdsi.webapi.vocabulary.Concept;

/**
 *
 * @author cknoll1
 */
public class ObservationPeriod extends Criteria {
  
  @JsonProperty("First")
  public Boolean first;

  @JsonProperty("PeriodStartDate")
  public DateRange periodStartDate;

  @JsonProperty("PeriodEndDate")
  public DateRange periodEndDate;
  
  @JsonProperty("PeriodType")
  public Concept[] periodType;
  
  @JsonProperty("PeriodLength")
  public NumericRange periodLength;  

  @JsonProperty("AgeAtStart")
  public NumericRange ageAtStart;  

  @JsonProperty("AgeAtEnd")
  public NumericRange ageAtEnd;  
  
  
  @Override
  public String accept(IGetCriteriaSqlDispatcher dispatcher) {
    return dispatcher.getCriteriaSql(this);
  }  
}
