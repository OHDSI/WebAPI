package org.ohdsi.webapi.check.checker.criteria;

import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.DateRange;
import org.ohdsi.circe.cohortdefinition.DoseEra;
import org.ohdsi.circe.cohortdefinition.NumericRange;
import org.ohdsi.webapi.check.builder.DateRangeValidatorBuilder;
import org.ohdsi.webapi.check.builder.NumericRangeValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;

public class DoseEraHelper {
    public static ValidatorGroupBuilder<Criteria, DoseEra> prepareDoseEraBuilder() {
        ValidatorGroupBuilder<Criteria, DoseEra> builder =
                new ValidatorGroupBuilder<Criteria, DoseEra>()
                        .attrName("dose era")
                        .conditionGetter(t -> t instanceof DoseEra)
                        .valueGetter(t -> (DoseEra) t)
                        .groups(
                                prepareAgeAtStartBuilder(),
                                prepareAgeAtEndBuilder(),
                                prepareStartDateBuilder(),
                                prepareEndDateBuilder(),
                                prepareLengthBuilder(),
                                prepareDoseValueBuilder()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<DoseEra, NumericRange> prepareAgeAtStartBuilder() {
        ValidatorGroupBuilder<DoseEra, NumericRange> builder =
                new ValidatorGroupBuilder<DoseEra, NumericRange>()
                        .attrName("age at start")
                        .valueGetter(t -> t.ageAtStart)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<DoseEra, NumericRange> prepareAgeAtEndBuilder() {
        ValidatorGroupBuilder<DoseEra, NumericRange> builder =
                new ValidatorGroupBuilder<DoseEra, NumericRange>()
                        .attrName("age at end")
                        .valueGetter(t -> t.ageAtEnd)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<DoseEra, DateRange> prepareStartDateBuilder() {
        ValidatorGroupBuilder<DoseEra, DateRange> builder =
                new ValidatorGroupBuilder<DoseEra, DateRange>()
                        .attrName("era start date")
                        .valueGetter(t -> t.eraStartDate)
                        .validators(
                                new DateRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<DoseEra, DateRange> prepareEndDateBuilder() {
        ValidatorGroupBuilder<DoseEra, DateRange> builder =
                new ValidatorGroupBuilder<DoseEra, DateRange>()
                        .attrName("era end date")
                        .valueGetter(t -> t.eraEndDate)
                        .validators(
                                new DateRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<DoseEra, NumericRange> prepareLengthBuilder() {
        ValidatorGroupBuilder<DoseEra, NumericRange> builder =
                new ValidatorGroupBuilder<DoseEra, NumericRange>()
                        .attrName("era length")
                        .valueGetter(t -> t.eraLength)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }

    public static ValidatorGroupBuilder<DoseEra, NumericRange> prepareDoseValueBuilder() {
        ValidatorGroupBuilder<DoseEra, NumericRange> builder =
                new ValidatorGroupBuilder<DoseEra, NumericRange>()
                        .attrName("dose value")
                        .valueGetter(t -> t.doseValue)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }
}
