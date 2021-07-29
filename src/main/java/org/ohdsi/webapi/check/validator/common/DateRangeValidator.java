package org.ohdsi.webapi.check.validator.common;

import static org.ohdsi.webapi.check.Comparisons.isDateValid;
import static org.ohdsi.webapi.check.operations.Operations.match;

import java.util.Objects;
import java.util.function.Function;
import org.ohdsi.circe.cohortdefinition.DateRange;
import org.ohdsi.webapi.check.Comparisons;
import org.ohdsi.webapi.check.validator.Context;
import org.ohdsi.webapi.check.validator.Path;
import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.warning.WarningSeverity;

public class DateRangeValidator<T extends DateRange> extends Validator<T> {
    private static final String EMPTY_START_VALUE = "empty start value";
    private static final String EMPTY_END_VALUE = "empty end value";
    private static final String START_GREATER_THAN_END = "start value greater than end";
    private static final String DATE_IS_INVALID = "invalid date value";

    public DateRangeValidator(Path path, WarningSeverity severity, String errorMessage) {

        super(path, severity, errorMessage);
    }

    @Override
    public boolean validate(DateRange range, Context context) {

        if (range == null) {
            return true;
        }

        Function<String, Boolean> warning = (message) -> {
            context.addWarning(severity, message, path);
            return false;
        };
        return match(range, true)
                .when(r -> Objects.nonNull(r.value) && !isDateValid(r.value))
                .thenReturn(x -> warning.apply(DATE_IS_INVALID))
                .when(r -> Objects.nonNull(r.op) && r.op.endsWith("bt"))
                .thenReturn(r -> match(r, true)
                        .when(x -> Objects.isNull(x.value))
                        .thenReturn(x -> warning.apply(EMPTY_START_VALUE))
                        .when(x -> Objects.isNull(x.extent))
                        .thenReturn(x -> warning.apply(EMPTY_END_VALUE))
                        .when(x -> Objects.nonNull(x.extent) && !isDateValid(x.extent))
                        .thenReturn(x -> warning.apply(DATE_IS_INVALID))
                        .when(Comparisons::startIsGreaterThanEnd)
                        .thenReturn(x -> warning.apply(START_GREATER_THAN_END))
                        .value())
                .orElseReturn(r -> match(r, true).when(x -> Objects.isNull(x.value))
                        .thenReturn(x -> warning.apply(EMPTY_START_VALUE))
                        .value()
                ).value();
    }
}
