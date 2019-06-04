package org.ohdsi.circe.cohortdefinition;

public interface IGetCriteriaSqlDispatcher {
    String getCriteriaSql(ConditionEra conditionEra);

    String getCriteriaSql(ConditionOccurrence conditionOccurrence);

    String getCriteriaSql(Death death);

    String getCriteriaSql(DeviceExposure deviceExposure);

    String getCriteriaSql(DoseEra doseEra);

    String getCriteriaSql(DrugEra drugEra);

    String getCriteriaSql(DrugExposure drugExposure);

    String getCriteriaSql(Measurement measurement);

    String getCriteriaSql(Observation observation);

    String getCriteriaSql(ObservationPeriod observationPeriod);

    String getCriteriaSql(PayerPlanPeriod payerPlanPeriod);

    String getCriteriaSql(ProcedureOccurrence procedureOccurrence);

    String getCriteriaSql(Specimen specimen);

    String getCriteriaSql(VisitOccurrence visitOccurrence);

    String getCriteriaSql(TreatmentLine treatmentLine);
}