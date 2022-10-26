package org.ohdsi.webapi.check.checker.criteria;

import org.ohdsi.circe.cohortdefinition.CorelatedCriteria;
import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;
import org.ohdsi.webapi.check.builder.criteria.CriteriaValidatorBuilder;
import org.ohdsi.webapi.check.warning.WarningSeverity;

import static org.ohdsi.webapi.check.checker.criteria.ConditionEraHelper.prepareConditionEraBuilder;
import static org.ohdsi.webapi.check.checker.criteria.ConditionOccurrenceHelper.prepareConditionOccurrenceBuilder;
import static org.ohdsi.webapi.check.checker.criteria.DeathHelper.prepareDeathBuilder;
import static org.ohdsi.webapi.check.checker.criteria.DeviceExposureHelper.prepareDeviceExposureBuilder;
import static org.ohdsi.webapi.check.checker.criteria.DoseEraHelper.prepareDoseEraBuilder;
import static org.ohdsi.webapi.check.checker.criteria.DrugEraHelper.prepareDrugEraBuilder;
import static org.ohdsi.webapi.check.checker.criteria.DrugExposureHelper.prepareDrugExposureBuilder;
import static org.ohdsi.webapi.check.checker.criteria.LocationRegionHelper.prepareLocationRegionBuilder;
import static org.ohdsi.webapi.check.checker.criteria.MeasurementHelper.prepareMeasurementBuilder;
import static org.ohdsi.webapi.check.checker.criteria.ObservationHelper.prepareObservationBuilder;
import static org.ohdsi.webapi.check.checker.criteria.ObservationPeriodHelper.prepareObservationPeriodBuilder;
import static org.ohdsi.webapi.check.checker.criteria.PayerPlanPeriodHelper.preparePayerPlanPeriodBuilder;
import static org.ohdsi.webapi.check.checker.criteria.ProcedureOccurrenceHelper.prepareProcedureOccurrenceBuilder;
import static org.ohdsi.webapi.check.checker.criteria.SpecimenHelper.prepareSpecimenBuilder;
import static org.ohdsi.webapi.check.checker.criteria.VisitDetailHelper.prepareVisitDetailBuilder;
import static org.ohdsi.webapi.check.checker.criteria.VisitOccurrenceHelper.prepareVisitOccurrenceBuilder;

public class CriteriaHelper {
    public static ValidatorGroupBuilder<CorelatedCriteria, Criteria> prepareCriteriaBuilder() {
        ValidatorGroupBuilder<CorelatedCriteria, Criteria> builder =
                new ValidatorGroupBuilder<CorelatedCriteria, Criteria>()
                        .valueGetter(t -> t.criteria)
                        .severity(WarningSeverity.CRITICAL)
                        .validators(
                                new CriteriaValidatorBuilder<>()
                        )
                        .groups(
                                prepareConditionEraBuilder(),
                                prepareConditionOccurrenceBuilder(),
                                prepareDeathBuilder(),
                                prepareDeviceExposureBuilder(),
                                prepareDoseEraBuilder(),
                                prepareDrugEraBuilder(),
                                prepareDrugExposureBuilder(),
                                prepareLocationRegionBuilder(),
                                prepareMeasurementBuilder(),
                                prepareObservationBuilder(),
                                prepareObservationPeriodBuilder(),
                                preparePayerPlanPeriodBuilder(),
                                prepareProcedureOccurrenceBuilder(),
                                prepareSpecimenBuilder(),
                                prepareVisitOccurrenceBuilder(),
                                prepareVisitDetailBuilder()
                        );
        return builder;
    }
}
