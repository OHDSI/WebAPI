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
  String visit(ConditionOccurrence conditionOccurrenceCriteria);
  String visit(DrugExposure drugExposureCriteria);
  String visit(ProcedureOccurrence procedureOccurrenceCriteria);
  String visit(ObservationPeriod observationPeriodCriteria);
  String visit(Measurement measurementCriteria);
  String visit(Observation observationCriteria);
  String visit(AdditionalCriteria additionalCriteria);
  String visit(CriteriaGroup criteriaGroup);
}
