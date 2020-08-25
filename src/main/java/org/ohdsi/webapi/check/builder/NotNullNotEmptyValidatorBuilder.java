package org.ohdsi.webapi.check.builder;

import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.validator.common.NotNullNotEmptyValidator;

public class NotNullNotEmptyValidatorBuilder<T> extends ValidatorBuilder<T> {

    @Override
    public Validator<T> build() {

        return new NotNullNotEmptyValidator<>(createChildPath(),  severity, errorMessage);
    }
}
