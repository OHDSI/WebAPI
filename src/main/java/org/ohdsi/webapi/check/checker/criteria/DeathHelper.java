package org.ohdsi.webapi.check.checker.criteria;

import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.DateRange;
import org.ohdsi.circe.cohortdefinition.Death;
import org.ohdsi.circe.cohortdefinition.NumericRange;
import org.ohdsi.webapi.check.builder.DateRangeValidatorBuilder;
import org.ohdsi.webapi.check.builder.NumericRangeValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;

public class DeathHelper {
    public static ValidatorGroupBuilder<Criteria, Death> prepareDeathBuilder() {
        ValidatorGroupBuilder<Criteria, Death> builder =
                new ValidatorGroupBuilder<Criteria, Death>()
                        .attrName("death")
                        .conditionGetter(t -> t instanceof Death)
                        .valueGetter(t -> (Death) t)
                        .groups(
                                prepareAgeBuilder(),
                                prepareOccurrenceStartDateBuilder()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<Death, NumericRange> prepareAgeBuilder() {
        ValidatorGroupBuilder<Death, NumericRange> builder =
                new ValidatorGroupBuilder<Death, NumericRange>()
                        .attrName("age")
                        .valueGetter(t -> t.age)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<Death, DateRange> prepareOccurrenceStartDateBuilder() {
        ValidatorGroupBuilder<Death, DateRange> builder =
                new ValidatorGroupBuilder<Death, DateRange>()
                        .attrName("occurrence start date")
                        .valueGetter(t -> t.occurrenceStartDate)
                        .validators(
                                new DateRangeValidatorBuilder<>()
                        );
        return builder;
    }
}
