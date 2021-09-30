package org.ohdsi.circe.cohortdefinition;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import org.ohdsi.circe.cohortdefinition.builders.BuilderOptions;

@JsonTypeInfo(
        use = Id.NAME,
        include = As.WRAPPER_OBJECT
)
@JsonSubTypes({@Type(
        value = ConditionEra.class,
        name = "ConditionEra"
), @Type(
        value = ConditionOccurrence.class,
        name = "ConditionOccurrence"
), @Type(
        value = Death.class,
        name = "Death"
), @Type(
        value = DeviceExposure.class,
        name = "DeviceExposure"
), @Type(
        value = DoseEra.class,
        name = "DoseEra"
), @Type(
        value = DrugEra.class,
        name = "DrugEra"
), @Type(
        value = DrugExposure.class,
        name = "DrugExposure"
), @Type(
        value = TreatmentLine.class,
        name = "TreatmentLine"
), @Type(
        value = Measurement.class,
        name = "Measurement"
), @Type(
        value = Observation.class,
        name = "Observation"
), @Type(
        value = ObservationPeriod.class,
        name = "ObservationPeriod"
), @Type(
        value = ProcedureOccurrence.class,
        name = "ProcedureOccurrence"
), @Type(
        value = Specimen.class,
        name = "Specimen"
), @Type(
        value = VisitOccurrence.class,
        name = "VisitOccurrence"
), @Type(
        value = PayerPlanPeriod.class,
        name = "PayerPlanPeriod"
)})
public abstract class Criteria {

    public String accept(IGetCriteriaSqlDispatcher dispatcher) {
        return this.accept(dispatcher, null);
    }

    public abstract String accept(IGetCriteriaSqlDispatcher dispatcher, BuilderOptions options);

    @JsonProperty("CorrelatedCriteria")
    public CriteriaGroup CorrelatedCriteria;
}