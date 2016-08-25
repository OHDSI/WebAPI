/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.cohortdefinition;

/**
 *
 * @author cknoll1
 */
interface IGetCriteriaSqlDispatcher {
  String getCriteriaSql(ConditionEra conditionEraCriteria);
  String getCriteriaSql(ConditionOccurrence conditionOccurrenceCriteria);
  String getCriteriaSql(Death deathCriteria);
  String getCriteriaSql(DeviceExposure deviceExposureCriteria);
  String getCriteriaSql(DoseEra doseEraCriteria);
  String getCriteriaSql(DrugEra drugEraCriteria);
  String getCriteriaSql(DrugExposure drugExposureCriteria);
  String getCriteriaSql(Measurement measurementCriteria);
  String getCriteriaSql(Observation observationCriteria);
  String getCriteriaSql(ObservationPeriod observationPeriodCriteria);
  String getCriteriaSql(ProcedureOccurrence procedureOccurrenceCriteria);
  String getCriteriaSql(Specimen specimenCriteria);
  String getCriteriaSql(VisitOccurrence specimenCriteria);
}
