package org.ohdsi.webapi.check.checker.criteria;

import org.ohdsi.circe.cohortdefinition.ConditionEra;
import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.DateRange;
import org.ohdsi.circe.cohortdefinition.NumericRange;
import org.ohdsi.webapi.check.builder.DateRangeValidatorBuilder;
import org.ohdsi.webapi.check.builder.NumericRangeValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;

public class ConditionEraHelper {
    public static ValidatorGroupBuilder<Criteria, ConditionEra> prepareConditionEraBuilder() {
        ValidatorGroupBuilder<Criteria, ConditionEra> builder =
                new ValidatorGroupBuilder<Criteria, ConditionEra>()
                        .attrName("condition era")
                        .conditionGetter(t -> t instanceof ConditionEra)
                        .valueGetter(t -> (ConditionEra) t)
                        .groups(
                                prepareAgeAtStartBuilder(),
                                prepareAgeAtEndBuilder(),
                                prepareStartDateBuilder(),
                                prepareEndDateBuilder(),
                                prepareLengthBuilder(),
                                prepareOccurrenceCountBuilder()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<ConditionEra, NumericRange> prepareAgeAtStartBuilder() {
        ValidatorGroupBuilder<ConditionEra, NumericRange> builder =
                new ValidatorGroupBuilder<ConditionEra, NumericRange>()
                        .attrName("age in years at era start")
                        .valueGetter(t -> t.ageAtStart)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<ConditionEra, NumericRange> prepareAgeAtEndBuilder() {
        ValidatorGroupBuilder<ConditionEra, NumericRange> builder =
                new ValidatorGroupBuilder<ConditionEra, NumericRange>()
                        .attrName("age in years at era end")
                        .valueGetter(t -> t.ageAtEnd)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<ConditionEra, DateRange> prepareStartDateBuilder() {
        ValidatorGroupBuilder<ConditionEra, DateRange> builder =
                new ValidatorGroupBuilder<ConditionEra, DateRange>()
                        .attrName("era start date")
                        .valueGetter(t -> t.eraStartDate)
                        .validators(
                                new DateRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<ConditionEra, DateRange> prepareEndDateBuilder() {
        ValidatorGroupBuilder<ConditionEra, DateRange> builder =
                new ValidatorGroupBuilder<ConditionEra, DateRange>()
                        .attrName("era end date")
                        .valueGetter(t -> t.eraEndDate)
                        .validators(
                                new DateRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<ConditionEra, NumericRange> prepareLengthBuilder() {
        ValidatorGroupBuilder<ConditionEra, NumericRange> builder =
                new ValidatorGroupBuilder<ConditionEra, NumericRange>()
                        .attrName("era length")
                        .valueGetter(t -> t.eraLength)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }

    public static ValidatorGroupBuilder<ConditionEra, NumericRange> prepareOccurrenceCountBuilder() {
        ValidatorGroupBuilder<ConditionEra, NumericRange> builder =
                new ValidatorGroupBuilder<ConditionEra, NumericRange>()
                        .attrName("occurrence count")
                        .valueGetter(t -> t.occurrenceCount)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }
}
