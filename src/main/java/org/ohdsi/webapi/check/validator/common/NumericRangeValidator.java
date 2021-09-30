package org.ohdsi.webapi.check.validator.common;

import static org.ohdsi.webapi.check.operations.Operations.match;

import java.util.Objects;
import java.util.function.Function;
import org.ohdsi.circe.cohortdefinition.NumericRange;
import org.ohdsi.webapi.check.Comparisons;
import org.ohdsi.webapi.check.validator.Context;
import org.ohdsi.webapi.check.validator.Path;
import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.warning.WarningSeverity;

public class NumericRangeValidator<T extends NumericRange> extends Validator<T> {
    private static final String EMPTY_START_VALUE = "empty start value";
    private static final String EMPTY_END_VALUE = "empty end value";
    private static final String START_GREATER_THAN_END = "start value greater than end";
    private static final String START_IS_NEGATIVE = "start value is negative";

    public NumericRangeValidator(Path path, WarningSeverity severity, String errorMessage) {

        super(path, severity, errorMessage);
    }

    @Override
    public boolean validate(NumericRange range, Context context) {

        if (range == null) {
            return true;
        }

        Function<String, Boolean> warning = (message) -> {
            context.addWarning(severity, message, path);
            return false;
        };
        return match(range, true)
                .when(Comparisons::isStartNegative)
                .thenReturn(x -> warning.apply(START_IS_NEGATIVE))
                .when(x -> Objects.isNull(x.value))
                .thenReturn(x -> warning.apply(EMPTY_START_VALUE))
                .when(r -> Objects.nonNull(r.op) && r.op.endsWith("bt"))
                .thenReturn(r ->
                        match(r, true)
                                .when(x -> Objects.isNull(x.extent))
                                .thenReturn(x -> warning.apply(EMPTY_END_VALUE))
                                .when(Comparisons::startIsGreaterThanEnd)
                                .thenReturn(x -> warning.apply(START_GREATER_THAN_END))
                                .value())
                .value();
    }
}
