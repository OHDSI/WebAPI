package org.ohdsi.webapi.check.builder;

import org.ohdsi.circe.cohortdefinition.Period;
import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.validator.common.PeriodValidator;

public class PeriodValidatorBuilder<T extends Period> extends ValidatorBuilder<T> {

    @Override
    public Validator<T> build() {
        return new PeriodValidator<>(createChildPath(), severity, errorMessage);
    }
}

