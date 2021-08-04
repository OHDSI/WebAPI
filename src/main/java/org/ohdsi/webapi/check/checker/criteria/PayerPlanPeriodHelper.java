package org.ohdsi.webapi.check.checker.criteria;

import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.DateRange;
import org.ohdsi.circe.cohortdefinition.NumericRange;
import org.ohdsi.circe.cohortdefinition.PayerPlanPeriod;
import org.ohdsi.circe.cohortdefinition.Period;
import org.ohdsi.webapi.check.builder.DateRangeValidatorBuilder;
import org.ohdsi.webapi.check.builder.NumericRangeValidatorBuilder;
import org.ohdsi.webapi.check.builder.PeriodValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;

public class PayerPlanPeriodHelper {
    public static ValidatorGroupBuilder<Criteria, PayerPlanPeriod> preparePayerPlanPeriodBuilder() {
        ValidatorGroupBuilder<Criteria, PayerPlanPeriod> builder =
                new ValidatorGroupBuilder<Criteria, PayerPlanPeriod>()
                        .attrName("payer plan period")
                        .conditionGetter(t -> t instanceof PayerPlanPeriod)
                        .valueGetter(t -> (PayerPlanPeriod) t)
                        .groups(
                                prepareStartDateBuilder(),
                                prepareEndDateBuilder(),
                                prepareLengthBuilder(),
                                prepareAgeAtStartBuilder(),
                                prepareAgeAtEndBuilder(),
                                prepareUserDefinedPeriod()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<PayerPlanPeriod, NumericRange> prepareAgeAtStartBuilder() {
        ValidatorGroupBuilder<PayerPlanPeriod, NumericRange> builder =
                new ValidatorGroupBuilder<PayerPlanPeriod, NumericRange>()
                        .attrName("age at start")
                        .valueGetter(t -> t.ageAtStart)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<PayerPlanPeriod, NumericRange> prepareAgeAtEndBuilder() {
        ValidatorGroupBuilder<PayerPlanPeriod, NumericRange> builder =
                new ValidatorGroupBuilder<PayerPlanPeriod, NumericRange>()
                        .attrName("age at end")
                        .valueGetter(t -> t.ageAtEnd)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<PayerPlanPeriod, DateRange> prepareStartDateBuilder() {
        ValidatorGroupBuilder<PayerPlanPeriod, DateRange> builder =
                new ValidatorGroupBuilder<PayerPlanPeriod, DateRange>()
                        .attrName("period start date")
                        .valueGetter(t -> t.periodStartDate)
                        .validators(
                                new DateRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<PayerPlanPeriod, DateRange> prepareEndDateBuilder() {
        ValidatorGroupBuilder<PayerPlanPeriod, DateRange> builder =
                new ValidatorGroupBuilder<PayerPlanPeriod, DateRange>()
                        .attrName("period end date")
                        .valueGetter(t -> t.periodEndDate)
                        .validators(
                                new DateRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<PayerPlanPeriod, NumericRange> prepareLengthBuilder() {
        ValidatorGroupBuilder<PayerPlanPeriod, NumericRange> builder =
                new ValidatorGroupBuilder<PayerPlanPeriod, NumericRange>()
                        .attrName("period length")
                        .valueGetter(t -> t.periodLength)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }

    public static ValidatorGroupBuilder<PayerPlanPeriod, Period> prepareUserDefinedPeriod() {
        ValidatorGroupBuilder<PayerPlanPeriod, Period> builder =
                new ValidatorGroupBuilder<PayerPlanPeriod, Period>()
                        .attrName("user defined period")
                        .valueGetter(t -> t.userDefinedPeriod)
                        .validators(
                                new PeriodValidatorBuilder<>()
                        );
        return builder;
    }
}
