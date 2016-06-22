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
interface ICohortExpressionElementVisitor {
  String visit(ConditionEra conditionEraCriteria);
  String visit(ConditionOccurrence conditionOccurrenceCriteria);
  String visit(Death deathCriteria);
  String visit(DeviceExposure deviceExposureCriteria);
  String visit(DoseEra doseEraCriteria);
  String visit(DrugEra drugEraCriteria);
  String visit(DrugExposure drugExposureCriteria);
  String visit(Measurement measurementCriteria);
  String visit(Observation observationCriteria);
  String visit(ObservationPeriod observationPeriodCriteria);
  String visit(ProcedureOccurrence procedureOccurrenceCriteria);
  String visit(Specimen specimenCriteria);
  String visit(VisitOccurrence specimenCriteria);
}
