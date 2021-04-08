package org.ohdsi.webapi.check.checker.criteria;

import org.ohdsi.circe.cohortdefinition.ConditionOccurrence;
import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.DateRange;
import org.ohdsi.circe.cohortdefinition.NumericRange;
import org.ohdsi.webapi.check.builder.DateRangeValidatorBuilder;
import org.ohdsi.webapi.check.builder.NumericRangeValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;

public class ConditionOccurrenceHelper {
    public static ValidatorGroupBuilder<Criteria, ConditionOccurrence> prepareConditionOccurrenceBuilder() {
        ValidatorGroupBuilder<Criteria, ConditionOccurrence> builder =
                new ValidatorGroupBuilder<Criteria, ConditionOccurrence>()
                        .attrName("condition occurence")
                        .conditionGetter(t -> t instanceof ConditionOccurrence)
                        .valueGetter(t -> (ConditionOccurrence) t)
                        .groups(
                                prepareAgeBuilder(),
                                prepareStartDateBuilder(),
                                prepareEndDateBuilder()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<ConditionOccurrence, NumericRange> prepareAgeBuilder() {
        ValidatorGroupBuilder<ConditionOccurrence, NumericRange> builder =
                new ValidatorGroupBuilder<ConditionOccurrence, NumericRange>()
                        .attrName("age")
                        .valueGetter(t -> t.age)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<ConditionOccurrence, DateRange> prepareStartDateBuilder() {
        ValidatorGroupBuilder<ConditionOccurrence, DateRange> builder =
                new ValidatorGroupBuilder<ConditionOccurrence, DateRange>()
                        .attrName("occurrence start date")
                        .valueGetter(t -> t.occurrenceStartDate)
                        .validators(
                                new DateRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<ConditionOccurrence, DateRange> prepareEndDateBuilder() {
        ValidatorGroupBuilder<ConditionOccurrence, DateRange> builder =
                new ValidatorGroupBuilder<ConditionOccurrence, DateRange>()
                        .attrName("occurrence end date")
                        .valueGetter(t -> t.occurrenceEndDate)
                        .validators(
                                new DateRangeValidatorBuilder<>()
                        );
        return builder;
    }
}
