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
public class DeviceExposure extends Criteria {

  @JsonProperty("CodesetId")
  public Integer codesetId;

  @JsonProperty("First")
  public Boolean first;

  @JsonProperty("OccurrenceStartDate")
  public DateRange occurrenceStartDate;

  @JsonProperty("OccurrenceEndDate")
  public DateRange occurrenceEndDate;

  @JsonProperty("DeviceType")
  public Concept[] deviceType;

  @JsonProperty("UniqueDeviceId")
  public TextFilter uniqueDeviceId;

  @JsonProperty("Quantity")
  public NumericRange quantity;

  @JsonProperty("DeviceSourceConcept")
  public Integer deviceSourceConcept;

  @JsonProperty("Age")
  public NumericRange age;

  @JsonProperty("Gender")
  public Concept[] gender;

  @JsonProperty("ProviderSpecialty")
  public Concept[] providerSpecialty;

  @JsonProperty("VisitType")
  public Concept[] visitType;

  @Override
  public String accept(IGetCriteriaSqlDispatcher dispatcher) {
    return dispatcher.getCriteriaSql(this);
  }

}
