package org.ohdsi.webapi.check.builder;

import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.validator.common.FakeTrueValidator;

public class FakeTrueValidatorBuilder<T> extends ValidatorBuilder<T> {
    @Override
    public Validator<T> build() {
        return new FakeTrueValidator<T>();
    }
}
