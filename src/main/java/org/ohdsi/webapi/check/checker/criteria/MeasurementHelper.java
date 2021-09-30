package org.ohdsi.webapi.check.checker.criteria;

import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.DateRange;
import org.ohdsi.circe.cohortdefinition.Measurement;
import org.ohdsi.circe.cohortdefinition.NumericRange;
import org.ohdsi.webapi.check.builder.DateRangeValidatorBuilder;
import org.ohdsi.webapi.check.builder.NumericRangeValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;

public class MeasurementHelper {
    public static ValidatorGroupBuilder<Criteria, Measurement> prepareMeasurementBuilder() {
        ValidatorGroupBuilder<Criteria, Measurement> builder =
                new ValidatorGroupBuilder<Criteria, Measurement>()
                        .attrName("measurement")
                        .conditionGetter(t -> t instanceof Measurement)
                        .valueGetter(t -> (Measurement) t)
                        .groups(
                                prepareOccurrenceStartDateBuilder(),
                                prepareValueAsNumberBuilder(),
                                prepareRangeLowBuilder(),
                                prepareRangeHighBuilder(),
                                prepareRangeLowRatioBuilder(),
                                prepareRangeHighRatioBuilder(),
                                prepareAgeBuilder()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<Measurement, DateRange> prepareOccurrenceStartDateBuilder() {
        ValidatorGroupBuilder<Measurement, DateRange> builder =
                new ValidatorGroupBuilder<Measurement, DateRange>()
                        .attrName("occurrence start date")
                        .valueGetter(t -> t.occurrenceStartDate)
                        .validators(
                                new DateRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<Measurement, NumericRange> prepareAgeBuilder() {
        ValidatorGroupBuilder<Measurement, NumericRange> builder =
                new ValidatorGroupBuilder<Measurement, NumericRange>()
                        .attrName("age")
                        .valueGetter(t -> t.age)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<Measurement, NumericRange> prepareValueAsNumberBuilder() {
        ValidatorGroupBuilder<Measurement, NumericRange> builder =
                new ValidatorGroupBuilder<Measurement, NumericRange>()
                        .attrName("value as number")
                        .valueGetter(t -> t.valueAsNumber)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<Measurement, NumericRange> prepareRangeLowBuilder() {
        ValidatorGroupBuilder<Measurement, NumericRange> builder =
                new ValidatorGroupBuilder<Measurement, NumericRange>()
                        .attrName("range low")
                        .valueGetter(t -> t.rangeLow)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<Measurement, NumericRange> prepareRangeHighBuilder() {
        ValidatorGroupBuilder<Measurement, NumericRange> builder =
                new ValidatorGroupBuilder<Measurement, NumericRange>()
                        .attrName("range high")
                        .valueGetter(t -> t.rangeHigh)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<Measurement, NumericRange> prepareRangeLowRatioBuilder() {
        ValidatorGroupBuilder<Measurement, NumericRange> builder =
                new ValidatorGroupBuilder<Measurement, NumericRange>()
                        .attrName("range low ratio")
                        .valueGetter(t -> t.rangeLowRatio)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<Measurement, NumericRange> prepareRangeHighRatioBuilder() {
        ValidatorGroupBuilder<Measurement, NumericRange> builder =
                new ValidatorGroupBuilder<Measurement, NumericRange>()
                        .attrName("range high ratio")
                        .valueGetter(t -> t.rangeHighRatio)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }
}
