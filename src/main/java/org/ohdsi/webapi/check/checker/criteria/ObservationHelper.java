package org.ohdsi.webapi.check.checker.criteria;

import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.DateRange;
import org.ohdsi.circe.cohortdefinition.NumericRange;
import org.ohdsi.circe.cohortdefinition.Observation;
import org.ohdsi.webapi.check.builder.DateRangeValidatorBuilder;
import org.ohdsi.webapi.check.builder.NumericRangeValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;

public class ObservationHelper {
    public static ValidatorGroupBuilder<Criteria, Observation> prepareObservationBuilder() {
        ValidatorGroupBuilder<Criteria, Observation> builder =
                new ValidatorGroupBuilder<Criteria, Observation>()
                        .attrName("Observation")
                        .conditionGetter(t -> t instanceof Observation)
                        .valueGetter(t -> (Observation) t)
                        .groups(
                                prepareOccurrenceStartDateBuilder(),
                                prepareValueAsNumberBuilder(),
                                prepareAgeBuilder()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<Observation, DateRange> prepareOccurrenceStartDateBuilder() {
        ValidatorGroupBuilder<Observation, DateRange> builder =
                new ValidatorGroupBuilder<Observation, DateRange>()
                        .attrName("occurrence start date")
                        .valueGetter(t -> t.occurrenceStartDate)
                        .validators(
                                new DateRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<Observation, NumericRange> prepareAgeBuilder() {
        ValidatorGroupBuilder<Observation, NumericRange> builder =
                new ValidatorGroupBuilder<Observation, NumericRange>()
                        .attrName("age")
                        .valueGetter(t -> t.age)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<Observation, NumericRange> prepareValueAsNumberBuilder() {
        ValidatorGroupBuilder<Observation, NumericRange> builder =
                new ValidatorGroupBuilder<Observation, NumericRange>()
                        .attrName("value as number")
                        .valueGetter(t -> t.valueAsNumber)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }
}
