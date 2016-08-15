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
public class Specimen extends Criteria {
  
  @JsonProperty("CodesetId")  
  public Integer codesetId;

  @JsonProperty("First")
  public Boolean first;

  @JsonProperty("OccurrenceStartDate")  
  public DateRange occurrenceStartDate;
  
  @JsonProperty("SpecimenType")  
  public Concept[] specimenType;

  @JsonProperty("Quantity")  
  public NumericRange quantity;

  @JsonProperty("Unit")  
  public Concept[] unit;

  @JsonProperty("AnatomicSite")  
  public Concept[] anatomicSite;

  @JsonProperty("DiseaseStatus")  
  public Concept[] diseaseStatus;
  
  @JsonProperty("SourceId")  
  public TextFilter sourceId;
  
  @JsonProperty("SpecimenSourceConcept")  
  public Integer specimenSourceConcept;

  @JsonProperty("Age")
  public NumericRange age;
  
  @JsonProperty("Gender")
  public Concept[] gender;

  @Override
  public String accept(IGetCriteriaSqlDispatcher dispatcher)
  {
    return dispatcher.getCriteriaSql(this);
  }  
  
}
