package org.ohdsi.webapi.check.checker.criteria;

import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.DateRange;
import org.ohdsi.circe.cohortdefinition.DrugEra;
import org.ohdsi.circe.cohortdefinition.NumericRange;
import org.ohdsi.webapi.check.builder.DateRangeValidatorBuilder;
import org.ohdsi.webapi.check.builder.NumericRangeValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;

public class DrugEraHelper {
    public static ValidatorGroupBuilder<Criteria, DrugEra> prepareDrugEraBuilder() {
        ValidatorGroupBuilder<Criteria, DrugEra> builder =
                new ValidatorGroupBuilder<Criteria, DrugEra>()
                        .attrName("drug era")
                        .conditionGetter(t -> t instanceof DrugEra)
                        .valueGetter(t -> (DrugEra) t)
                        .groups(
                                prepareAgeAtStartBuilder(),
                                prepareAgeAtEndBuilder(),
                                prepareStartDateBuilder(),
                                prepareEndDateBuilder(),
                                prepareLengthBuilder(),
                                prepareOccurrenceCountBuilder(),
                                prepareGapDaysBuilder()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<DrugEra, NumericRange> prepareAgeAtStartBuilder() {
        ValidatorGroupBuilder<DrugEra, NumericRange> builder =
                new ValidatorGroupBuilder<DrugEra, NumericRange>()
                        .attrName("age at start")
                        .valueGetter(t -> t.ageAtStart)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<DrugEra, NumericRange> prepareAgeAtEndBuilder() {
        ValidatorGroupBuilder<DrugEra, NumericRange> builder =
                new ValidatorGroupBuilder<DrugEra, NumericRange>()
                        .attrName("age at end")
                        .valueGetter(t -> t.ageAtEnd)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<DrugEra, DateRange> prepareStartDateBuilder() {
        ValidatorGroupBuilder<DrugEra, DateRange> builder =
                new ValidatorGroupBuilder<DrugEra, DateRange>()
                        .attrName("era start date")
                        .valueGetter(t -> t.eraStartDate)
                        .validators(
                                new DateRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<DrugEra, DateRange> prepareEndDateBuilder() {
        ValidatorGroupBuilder<DrugEra, DateRange> builder =
                new ValidatorGroupBuilder<DrugEra, DateRange>()
                        .attrName("era end date")
                        .valueGetter(t -> t.eraEndDate)
                        .validators(
                                new DateRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<DrugEra, NumericRange> prepareLengthBuilder() {
        ValidatorGroupBuilder<DrugEra, NumericRange> builder =
                new ValidatorGroupBuilder<DrugEra, NumericRange>()
                        .attrName("era length")
                        .valueGetter(t -> t.eraLength)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }

    public static ValidatorGroupBuilder<DrugEra, NumericRange> prepareOccurrenceCountBuilder() {
        ValidatorGroupBuilder<DrugEra, NumericRange> builder =
                new ValidatorGroupBuilder<DrugEra, NumericRange>()
                        .attrName("occurrence count")
                        .valueGetter(t -> t.occurrenceCount)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }

    public static ValidatorGroupBuilder<DrugEra, NumericRange> prepareGapDaysBuilder() {
        ValidatorGroupBuilder<DrugEra, NumericRange> builder =
                new ValidatorGroupBuilder<DrugEra, NumericRange>()
                        .attrName("gap days")
                        .valueGetter(t -> t.gapDays)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }
}
