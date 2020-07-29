package org.ohdsi.webapi.check.builder;

import java.util.Collection;
import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.validator.common.IterableForEachValidator;

public class IterableForEachValidatorBuilder<T> extends ValidatorBuilder<Collection<? extends T>> {

    private ValidatorBuilder<T> validatorBuilder;

    public IterableForEachValidatorBuilder<T> validator(ValidatorBuilder<T> validatorBuilder) {
        this.validatorBuilder = validatorBuilder;
        return this;
    }

    @Override
    public Validator<Collection<? extends T>> build() {
        return new IterableForEachValidator<>(createChildPath(), severity, errorMessage, validatorBuilder.build());
    }

}
