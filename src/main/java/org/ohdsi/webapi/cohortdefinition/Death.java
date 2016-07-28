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
public class Death extends Criteria {
  @JsonProperty("CodesetId")
  public Integer codesetId;

  @JsonProperty("OccurrenceStartDate")
  public DateRange occurrenceStartDate;

  @JsonProperty("DeathType")
  public Concept[] deathType;

  @JsonProperty("DeathSourceConcept")
  public Integer deathSourceConcept;

  @JsonProperty("Age")
  public NumericRange age;
  
  @JsonProperty("Gender")
  public Concept[] gender;
  
  @Override
  public String accept(IGetCriteriaSqlDispatcher dispatcher) {
    return dispatcher.getCriteriaSql(this);
  }
  
}
