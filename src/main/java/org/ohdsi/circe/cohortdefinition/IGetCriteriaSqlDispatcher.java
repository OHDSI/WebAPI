package org.ohdsi.circe.cohortdefinition;

import org.ohdsi.circe.cohortdefinition.builders.BuilderOptions;

public interface IGetCriteriaSqlDispatcher {
    String getCriteriaSql(LocationRegion locationRegion, BuilderOptions options);
    String getCriteriaSql(ConditionEra conditionEraCriteria, BuilderOptions options);
    String getCriteriaSql(ConditionOccurrence conditionOccurrenceCriteria, BuilderOptions options);
    String getCriteriaSql(Death deathCriteria, BuilderOptions options);
    String getCriteriaSql(DeviceExposure deviceExposureCriteria, BuilderOptions options);
    String getCriteriaSql(DoseEra doseEraCriteria, BuilderOptions options);
    String getCriteriaSql(DrugEra drugEraCriteria, BuilderOptions options);
    String getCriteriaSql(DrugExposure drugExposureCriteria, BuilderOptions options);
    String getCriteriaSql(Measurement measurementCriteria, BuilderOptions options);
    String getCriteriaSql(Observation observationCriteria, BuilderOptions options);
    String getCriteriaSql(ObservationPeriod observationPeriodCriteria, BuilderOptions options);
    String getCriteriaSql(PayerPlanPeriod payerPlanPeriodCriteria, BuilderOptions options);
    String getCriteriaSql(ProcedureOccurrence procedureOccurrenceCriteria, BuilderOptions options);
    String getCriteriaSql(Specimen specimenCriteria, BuilderOptions options);
    String getCriteriaSql(VisitOccurrence visitOccurrenceCriteria, BuilderOptions options);
    String getCriteriaSql(TreatmentLine treatmentLine, BuilderOptions options);
}
