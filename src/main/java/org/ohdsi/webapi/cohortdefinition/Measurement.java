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
public class Measurement extends Criteria {
  
  @JsonProperty("CodesetId")  
  public Integer codesetId;
  
  @JsonProperty("First")
  public Boolean first;
  
  @JsonProperty("OccurrenceStartDate")
  public DateRange occurrenceStartDate;

  @JsonProperty("MeasurementType")
  public Concept[] measurementType;

  @JsonProperty("Operator")
  public Concept[] operator;

  @JsonProperty("ValueAsNumber")
  public NumericRange valueAsNumber;

  @JsonProperty("ValueAsConcept")
  public Concept[] valueAsConcept;
  
  @JsonProperty("Unit")
  public Concept[] unit;
  
  @JsonProperty("RangeLow")
  public NumericRange rangeLow;

  @JsonProperty("RangeHigh")
  public NumericRange rangeHigh;

  @JsonProperty("RangeLowRatio")
  public NumericRange rangeLowRatio;

  @JsonProperty("RangeHighRatio")
  public NumericRange rangeHighRatio;

  @JsonProperty("Abnormal")
  public Boolean abnormal;
  
  @JsonProperty("MeasurementSourceConcept")
  public Integer measurementSourceConcept;
  
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
