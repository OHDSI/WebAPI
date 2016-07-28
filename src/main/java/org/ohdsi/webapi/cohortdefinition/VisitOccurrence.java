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
public class VisitOccurrence extends Criteria {
  
  @JsonProperty("CodesetId")  
  public Integer codesetId;
  
  @JsonProperty("First")
  public Boolean first;
  
  @JsonProperty("OccurrenceStartDate")
  public DateRange occurrenceStartDate;

  @JsonProperty("OccurrenceEndDate")
  public DateRange occurrenceEndDate;

  @JsonProperty("VisitType")
  public Concept[] visitType;

  @JsonProperty("VisitSourceConcept")
  public Integer visitSourceConcept;

  @JsonProperty("VisitLength")
  public NumericRange visitLength;
  
  @JsonProperty("Age")
  public NumericRange age;
  
  @JsonProperty("Gender")
  public Concept[] gender;
  
  @JsonProperty("ProviderSpecialty")
  public Concept[] providerSpecialty;

  @JsonProperty("PlaceOfService")
  public Concept[] placeOfService;

  
  @Override
  public String accept(IGetCriteriaSqlDispatcher dispatcher)
  {
    return dispatcher.getCriteriaSql(this);
  }
  
  
}
