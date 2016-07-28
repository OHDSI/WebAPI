/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.cohortdefinition;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.ohdsi.webapi.vocabulary.Concept;

/**
 *
 * @author cknoll1
 */
@JsonTypeName("DrugExposure")
public class DrugExposure extends Criteria {
  @JsonProperty("CodesetId")  
  public Integer codesetId;
  
  @JsonProperty("First")
  public Boolean first;
  
  @JsonProperty("OccurrenceStartDate")
  public DateRange occurrenceStartDate;

  @JsonProperty("OccurrenceEndDate")
  public DateRange occurrenceEndDate;

  @JsonProperty("DrugType")
  public Concept[] drugType;

  @JsonProperty("StopReason")
  public TextFilter stopReason;
  
  @JsonProperty("Refills")
  public NumericRange refills;
  
  @JsonProperty("Quantity")
  public NumericRange quantity;
  
  @JsonProperty("DaysSupply")
  public NumericRange daysSupply;  
  
  @JsonProperty("RouteConcept")
  public Concept[] routeConcept;

  @JsonProperty("EffectiveDrugDose")
  public NumericRange effectiveDrugDose;  

  @JsonProperty("DoseUnit")
  public Concept[] doseUnit;

  @JsonProperty("LotNumber")
  public TextFilter lotNumber;  

  @JsonProperty("DrugSourceConcept")
  public Integer drugSourceConcept;
  
  @JsonProperty("Age")
  public NumericRange age;
  
  @JsonProperty("Gender")
  public Concept[] gender;
  
  @JsonProperty("ProviderSpecialty")
  public Concept[] providerSpecialty;

  @JsonProperty("VisitType")
  public Concept[] visitType;

  @Override
  public String accept(IGetCriteriaSqlDispatcher dispatcher)
  {
    return dispatcher.getCriteriaSql(this);
  }  
}
