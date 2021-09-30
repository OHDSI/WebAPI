package org.ohdsi.webapi.check.builder;

import org.ohdsi.circe.cohortdefinition.DateRange;
import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.validator.common.DateRangeValidator;

public class DateRangeValidatorBuilder<T extends DateRange> extends ValidatorBuilder<T> {

    @Override
    public Validator<T> build() {
        return new DateRangeValidator<>(createChildPath(),  severity, errorMessage);
    }
}

