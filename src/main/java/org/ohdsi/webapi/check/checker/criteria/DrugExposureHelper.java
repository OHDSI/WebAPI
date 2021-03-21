package org.ohdsi.webapi.check.checker.criteria;

import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.DateRange;
import org.ohdsi.circe.cohortdefinition.DrugExposure;
import org.ohdsi.circe.cohortdefinition.NumericRange;
import org.ohdsi.webapi.check.builder.DateRangeValidatorBuilder;
import org.ohdsi.webapi.check.builder.NumericRangeValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;

public class DrugExposureHelper {
    public static ValidatorGroupBuilder<Criteria, DrugExposure> prepareDrugExposureBuilder() {
        ValidatorGroupBuilder<Criteria, DrugExposure> builder =
                new ValidatorGroupBuilder<Criteria, DrugExposure>()
                        .attrName("drug exposure")
                        .conditionGetter(t -> t instanceof DrugExposure)
                        .valueGetter(t -> (DrugExposure) t)
                        .groups(
                                prepareAgeBuilder(),
                                prepareStartDateBuilder(),
                                prepareEndDateBuilder(),
                                prepareRefillsBuilder(),
                                prepareQuantityBuilder(),
                                prepareDaysSupplyBuilder(),
                                prepareEffectiveDrugDoseBuilder()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<DrugExposure, NumericRange> prepareAgeBuilder() {
        ValidatorGroupBuilder<DrugExposure, NumericRange> builder =
                new ValidatorGroupBuilder<DrugExposure, NumericRange>()
                        .attrName("age")
                        .valueGetter(t -> t.age)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<DrugExposure, DateRange> prepareStartDateBuilder() {
        ValidatorGroupBuilder<DrugExposure, DateRange> builder =
                new ValidatorGroupBuilder<DrugExposure, DateRange>()
                        .attrName("occurrence start date")
                        .valueGetter(t -> t.occurrenceStartDate)
                        .validators(
                                new DateRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<DrugExposure, DateRange> prepareEndDateBuilder() {
        ValidatorGroupBuilder<DrugExposure, DateRange> builder =
                new ValidatorGroupBuilder<DrugExposure, DateRange>()
                        .attrName("occurrence end date")
                        .valueGetter(t -> t.occurrenceEndDate)
                        .validators(
                                new DateRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<DrugExposure, NumericRange> prepareRefillsBuilder() {
        ValidatorGroupBuilder<DrugExposure, NumericRange> builder =
                new ValidatorGroupBuilder<DrugExposure, NumericRange>()
                        .attrName("refills")
                        .valueGetter(t -> t.refills)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<DrugExposure, NumericRange> prepareQuantityBuilder() {
        ValidatorGroupBuilder<DrugExposure, NumericRange> builder =
                new ValidatorGroupBuilder<DrugExposure, NumericRange>()
                        .attrName("quantity")
                        .valueGetter(t -> t.quantity)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<DrugExposure, NumericRange> prepareDaysSupplyBuilder() {
        ValidatorGroupBuilder<DrugExposure, NumericRange> builder =
                new ValidatorGroupBuilder<DrugExposure, NumericRange>()
                        .attrName("days supply")
                        .valueGetter(t -> t.daysSupply)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }

    public static ValidatorGroupBuilder<DrugExposure, NumericRange> prepareEffectiveDrugDoseBuilder() {
        ValidatorGroupBuilder<DrugExposure, NumericRange> builder =
                new ValidatorGroupBuilder<DrugExposure, NumericRange>()
                        .attrName("effective drug dose")
                        .valueGetter(t -> t.effectiveDrugDose)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }
}
