package org.ohdsi.webapi.check.checker.criteria;

import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.DateRange;
import org.ohdsi.circe.cohortdefinition.NumericRange;
import org.ohdsi.circe.cohortdefinition.VisitOccurrence;
import org.ohdsi.webapi.check.builder.DateRangeValidatorBuilder;
import org.ohdsi.webapi.check.builder.NumericRangeValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;

public class VisitOccurrenceHelper {
    public static ValidatorGroupBuilder<Criteria, VisitOccurrence> prepareVisitOccurrenceBuilder() {
        ValidatorGroupBuilder<Criteria, VisitOccurrence> builder =
                new ValidatorGroupBuilder<Criteria, VisitOccurrence>()
                        .attrName("VisitOccurrence")
                        .conditionGetter(t -> t instanceof VisitOccurrence)
                        .valueGetter(t -> (VisitOccurrence) t)
                        .groups(
                                prepareAgeBuilder(),
                                prepareStartDateBuilder(),
                                prepareEndDateBuilder(),
                                prepareVisitLengthBuilder()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<VisitOccurrence, NumericRange> prepareAgeBuilder() {
        ValidatorGroupBuilder<VisitOccurrence, NumericRange> builder =
                new ValidatorGroupBuilder<VisitOccurrence, NumericRange>()
                        .attrName("age")
                        .valueGetter(t -> t.age)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<VisitOccurrence, DateRange> prepareStartDateBuilder() {
        ValidatorGroupBuilder<VisitOccurrence, DateRange> builder =
                new ValidatorGroupBuilder<VisitOccurrence, DateRange>()
                        .attrName("occurrence start date")
                        .valueGetter(t -> t.occurrenceStartDate)
                        .validators(
                                new DateRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<VisitOccurrence, DateRange> prepareEndDateBuilder() {
        ValidatorGroupBuilder<VisitOccurrence, DateRange> builder =
                new ValidatorGroupBuilder<VisitOccurrence, DateRange>()
                        .attrName("occurrence end date")
                        .valueGetter(t -> t.occurrenceEndDate)
                        .validators(
                                new DateRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<VisitOccurrence, NumericRange> prepareVisitLengthBuilder() {
        ValidatorGroupBuilder<VisitOccurrence, NumericRange> builder =
                new ValidatorGroupBuilder<VisitOccurrence, NumericRange>()
                        .attrName("visit length")
                        .valueGetter(t -> t.visitLength)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }
}
