package org.ohdsi.webapi.check.checker.criteria;

import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.DateRange;
import org.ohdsi.circe.cohortdefinition.NumericRange;
import org.ohdsi.circe.cohortdefinition.ObservationPeriod;
import org.ohdsi.circe.cohortdefinition.Period;
import org.ohdsi.webapi.check.builder.DateRangeValidatorBuilder;
import org.ohdsi.webapi.check.builder.NumericRangeValidatorBuilder;
import org.ohdsi.webapi.check.builder.PeriodValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;

public class ObservationPeriodHelper {
    public static ValidatorGroupBuilder<Criteria, ObservationPeriod> prepareObservationPeriodBuilder() {
        ValidatorGroupBuilder<Criteria, ObservationPeriod> builder =
                new ValidatorGroupBuilder<Criteria, ObservationPeriod>()
                        .attrName("dose era")
                        .conditionGetter(t -> t instanceof ObservationPeriod)
                        .valueGetter(t -> (ObservationPeriod) t)
                        .groups(
                                prepareAgeAtStartBuilder(),
                                prepareAgeAtEndBuilder(),
                                preparePeriodStartBuilder(),
                                preparePeriodEndBuilder(),
                                preparePeriodLengthBuilder(),
                                prepareUserDefinedPeriodBuilder()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<ObservationPeriod, NumericRange> prepareAgeAtStartBuilder() {
        ValidatorGroupBuilder<ObservationPeriod, NumericRange> builder =
                new ValidatorGroupBuilder<ObservationPeriod, NumericRange>()
                        .attrName("age at start")
                        .valueGetter(t -> t.ageAtStart)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<ObservationPeriod, NumericRange> prepareAgeAtEndBuilder() {
        ValidatorGroupBuilder<ObservationPeriod, NumericRange> builder =
                new ValidatorGroupBuilder<ObservationPeriod, NumericRange>()
                        .attrName("age at end")
                        .valueGetter(t -> t.ageAtEnd)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<ObservationPeriod, DateRange> preparePeriodStartBuilder() {
        ValidatorGroupBuilder<ObservationPeriod, DateRange> builder =
                new ValidatorGroupBuilder<ObservationPeriod, DateRange>()
                        .attrName("period start date")
                        .valueGetter(t -> t.periodStartDate)
                        .validators(
                                new DateRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<ObservationPeriod, DateRange> preparePeriodEndBuilder() {
        ValidatorGroupBuilder<ObservationPeriod, DateRange> builder =
                new ValidatorGroupBuilder<ObservationPeriod, DateRange>()
                        .attrName("period end date")
                        .valueGetter(t -> t.periodEndDate)
                        .validators(
                                new DateRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<ObservationPeriod, NumericRange> preparePeriodLengthBuilder() {
        ValidatorGroupBuilder<ObservationPeriod, NumericRange> builder =
                new ValidatorGroupBuilder<ObservationPeriod, NumericRange>()
                        .attrName("period length")
                        .valueGetter(t -> t.periodLength)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }

    public static ValidatorGroupBuilder<ObservationPeriod, Period> prepareUserDefinedPeriodBuilder() {
        ValidatorGroupBuilder<ObservationPeriod, Period> builder =
                new ValidatorGroupBuilder<ObservationPeriod, Period>()
                        .attrName("user defined period")
                        .valueGetter(t -> t.userDefinedPeriod)
                        .validators(
                                new PeriodValidatorBuilder<>()
                        );
        return builder;
    }
}
