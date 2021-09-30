package org.ohdsi.webapi.check.checker.criteria;

import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.DateRange;
import org.ohdsi.circe.cohortdefinition.NumericRange;
import org.ohdsi.circe.cohortdefinition.Specimen;
import org.ohdsi.webapi.check.builder.DateRangeValidatorBuilder;
import org.ohdsi.webapi.check.builder.NumericRangeValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;

public class SpecimenHelper {
    public static ValidatorGroupBuilder<Criteria, Specimen> prepareSpecimenBuilder() {
        ValidatorGroupBuilder<Criteria, Specimen> builder =
                new ValidatorGroupBuilder<Criteria, Specimen>()
                        .attrName("specimen")
                        .conditionGetter(t -> t instanceof Specimen)
                        .valueGetter(t -> (Specimen) t)
                        .groups(
                                prepareAgeBuilder(),
                                prepareStartDateBuilder(),
                                prepareQuantityBuilder()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<Specimen, NumericRange> prepareAgeBuilder() {
        ValidatorGroupBuilder<Specimen, NumericRange> builder =
                new ValidatorGroupBuilder<Specimen, NumericRange>()
                        .attrName("age")
                        .valueGetter(t -> t.age)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<Specimen, DateRange> prepareStartDateBuilder() {
        ValidatorGroupBuilder<Specimen, DateRange> builder =
                new ValidatorGroupBuilder<Specimen, DateRange>()
                        .attrName("occurrence start date")
                        .valueGetter(t -> t.occurrenceStartDate)
                        .validators(
                                new DateRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<Specimen, NumericRange> prepareQuantityBuilder() {
        ValidatorGroupBuilder<Specimen, NumericRange> builder =
                new ValidatorGroupBuilder<Specimen, NumericRange>()
                        .attrName("quantity")
                        .valueGetter(t -> t.quantity)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }
}
