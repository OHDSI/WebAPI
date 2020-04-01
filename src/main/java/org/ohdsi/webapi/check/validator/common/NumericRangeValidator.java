package org.ohdsi.webapi.check.validator.common;

import org.ohdsi.circe.cohortdefinition.NumericRange;
import org.ohdsi.webapi.check.Comparisons;
import org.ohdsi.webapi.check.validator.Validator;

import java.util.Objects;
import java.util.function.Function;

import static org.ohdsi.webapi.check.operations.Operations.match;

public class NumericRangeValidator<T extends NumericRange> extends Validator<NumericRange> {
    private static final String EMPTY_START_VALUE = "%s - empty start value";
    private static final String EMPTY_END_VALUE = "%s - empty end value";
    private static final String START_GREATER_THAN_END = "%s - start value greater than end";
    private static final String START_IS_NEGATIVE = "%s - start value is negative";

    @Override
    public boolean validate(NumericRange range) {
        Function<String, Boolean> warning = (t) -> {
            reporter.add(this.severity, t, this.path.getPath());
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
