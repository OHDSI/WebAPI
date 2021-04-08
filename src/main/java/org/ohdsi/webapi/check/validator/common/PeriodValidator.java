package org.ohdsi.webapi.check.validator.common;

import org.ohdsi.circe.cohortdefinition.Period;
import org.ohdsi.webapi.check.Comparisons;
import org.ohdsi.webapi.check.validator.Context;
import org.ohdsi.webapi.check.validator.Path;
import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.warning.WarningSeverity;

import java.util.Objects;
import java.util.function.Function;

import static org.ohdsi.webapi.check.Comparisons.isDateValid;
import static org.ohdsi.webapi.check.operations.Operations.match;

public class PeriodValidator<T extends Period> extends Validator<T> {
    private static final String START_GREATER_THAN_END = "start value greater than end";
    private static final String DATE_IS_INVALID = "invalid date value";

    public PeriodValidator(Path path, WarningSeverity severity, String errorMessage) {

        super(path, severity, errorMessage);
    }

    @Override
    public boolean validate(Period period, Context context) {
        if (period == null) {
            return true;
        }

        Function<String, Boolean> warning = (message) -> {
            context.addWarning(severity, message, path);
            return false;
        };
        return match(period, true)
                .when(x -> Objects.nonNull(x.startDate) && !isDateValid(x.startDate))
                .thenReturn(x -> warning.apply(DATE_IS_INVALID))
                .when(x -> Objects.nonNull(x.endDate) && !isDateValid(x.endDate))
                .thenReturn(x -> warning.apply(DATE_IS_INVALID))
                .when(Comparisons::startIsGreaterThanEnd)
                .thenReturn(x -> warning.apply(START_GREATER_THAN_END))
                .value();
    }
}
