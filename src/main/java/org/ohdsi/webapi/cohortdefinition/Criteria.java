/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.cohortdefinition;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.WRAPPER_OBJECT)
@JsonSubTypes({
  @JsonSubTypes.Type(value = ConditionEra.class, name = "ConditionEra"),
  @JsonSubTypes.Type(value = ConditionOccurrence.class, name = "ConditionOccurrence"),
  @JsonSubTypes.Type(value = Death.class, name = "Death"),
  @JsonSubTypes.Type(value = DeviceExposure.class, name = "DeviceExposure"),
  @JsonSubTypes.Type(value = DoseEra.class, name = "DoseEra"),
  @JsonSubTypes.Type(value = DrugEra.class, name = "DrugEra"),
  @JsonSubTypes.Type(value = DrugExposure.class, name = "DrugExposure"),
  @JsonSubTypes.Type(value = Measurement.class, name = "Measurement"),
  @JsonSubTypes.Type(value = Observation.class, name = "Observation"),
  @JsonSubTypes.Type(value = ObservationPeriod.class, name = "ObservationPeriod"),
  @JsonSubTypes.Type(value = ProcedureOccurrence.class, name = "ProcedureOccurrence"),
  @JsonSubTypes.Type(value = Specimen.class, name = "Specimen"),
  @JsonSubTypes.Type(value = VisitOccurrence.class, name = "VisitOccurrence")
})
public abstract class Criteria {
  public abstract String accept(IGetCriteriaSqlDispatcher dispatcher);
}
