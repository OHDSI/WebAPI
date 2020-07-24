package org.ohdsi.webapi.check.builder;

import org.ohdsi.circe.cohortdefinition.DateRange;
import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.validator.common.DateRangeValidator;

public class DateRangeValidatorBuilder<T extends DateRange> extends ValidatorBuilder<DateRange> {

    @Override
    public Validator<DateRange> build() {
        return new DateRangeValidator<>(createChildPath(),  severity, errorMessage);
    }
}

