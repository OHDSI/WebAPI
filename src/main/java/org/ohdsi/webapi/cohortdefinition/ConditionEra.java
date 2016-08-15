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
public class ConditionEra extends Criteria {

  @JsonProperty("CodesetId")  
  public Integer codesetId;

  @JsonProperty("First")  
  public Boolean first;
  
  @JsonProperty("EraStartDate")  
  public DateRange eraStartDate;

  @JsonProperty("EraEndDate")  
  public DateRange eraEndDate;

  @JsonProperty("OccurrenceCount")  
  public NumericRange occurrenceCount;

  @JsonProperty("EraLength")  
  public NumericRange eraLength;

  @JsonProperty("AgeAtStart")
  public NumericRange ageAtStart;  

  @JsonProperty("AgeAtEnd")
  public NumericRange ageAtEnd;  

  @JsonProperty("Gender")
  public Concept[] gender;  
  
  
  @Override
  public String accept(IGetCriteriaSqlDispatcher dispatcher) {
    return dispatcher.getCriteriaSql(this);
  }
  
}
