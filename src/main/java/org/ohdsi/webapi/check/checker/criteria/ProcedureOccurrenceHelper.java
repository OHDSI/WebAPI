package org.ohdsi.webapi.check.checker.criteria;

import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.DateRange;
import org.ohdsi.circe.cohortdefinition.NumericRange;
import org.ohdsi.circe.cohortdefinition.ProcedureOccurrence;
import org.ohdsi.webapi.check.builder.DateRangeValidatorBuilder;
import org.ohdsi.webapi.check.builder.NumericRangeValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;

public class ProcedureOccurrenceHelper {
    public static ValidatorGroupBuilder<Criteria, ProcedureOccurrence> prepareProcedureOccurrenceBuilder() {
        ValidatorGroupBuilder<Criteria, ProcedureOccurrence> builder =
                new ValidatorGroupBuilder<Criteria, ProcedureOccurrence>()
                        .attrName("procedure occurrence")
                        .conditionGetter(t -> t instanceof ProcedureOccurrence)
                        .valueGetter(t -> (ProcedureOccurrence) t)
                        .groups(
                                prepareAgeBuilder(),
                                prepareStartDateBuilder(),
                                prepareQuantityBuilder()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<ProcedureOccurrence, NumericRange> prepareAgeBuilder() {
        ValidatorGroupBuilder<ProcedureOccurrence, NumericRange> builder =
                new ValidatorGroupBuilder<ProcedureOccurrence, NumericRange>()
                        .attrName("age")
                        .valueGetter(t -> t.age)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<ProcedureOccurrence, DateRange> prepareStartDateBuilder() {
        ValidatorGroupBuilder<ProcedureOccurrence, DateRange> builder =
                new ValidatorGroupBuilder<ProcedureOccurrence, DateRange>()
                        .attrName("occurrence start date")
                        .valueGetter(t -> t.occurrenceStartDate)
                        .validators(
                                new DateRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<ProcedureOccurrence, NumericRange> prepareQuantityBuilder() {
        ValidatorGroupBuilder<ProcedureOccurrence, NumericRange> builder =
                new ValidatorGroupBuilder<ProcedureOccurrence, NumericRange>()
                        .attrName("quantity")
                        .valueGetter(t -> t.quantity)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }
}
