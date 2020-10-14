package org.ohdsi.webapi.check.builder;

import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.validator.common.IterableForEachValidator;

import java.util.Collection;
import java.util.Objects;

public class IterableForEachValidatorBuilder<T> extends ValidatorBuilder<Collection<? extends T>> {

    private ValidatorBuilder<T> validatorBuilder;

    public IterableForEachValidatorBuilder<T> validator(ValidatorBuilder<T> validatorBuilder) {
        this.validatorBuilder = validatorBuilder;
        return this;
    }

    @Override
    public Validator<Collection<? extends T>> build() {
        if (validatorBuilder.getBasePath() == null) {
            validatorBuilder.basePath(createChildPath());
        }
        if (validatorBuilder.getErrorMessage() == null) {
            validatorBuilder.errorMessage(this.errorMessage);

        }
        if (validatorBuilder.getSeverity() == null) {
            validatorBuilder.severity(severity);
        }
        if (validatorBuilder.getAttrName() == null) {
            validatorBuilder.attrName(attrName);
        }
        return new IterableForEachValidator<>(createChildPath(), severity, errorMessage, validatorBuilder.build());
    }

}
