package org.ohdsi.webapi.check.builder;

import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.validator.ValidatorGroup;
import org.ohdsi.webapi.check.validator.common.IterableForEachValidator;

import java.util.Collection;
import java.util.List;

public class IterableForEachValidatorBuilder<T> extends AbstractForEachValidatorBuilder<T, Collection<? extends T>> {
    @Override
    public Validator<Collection<? extends T>> build() {
        List<ValidatorGroup<T, ?>> groups = initGroups();
        /* List<Validator<T>> validators = initValidators();    MDACA Spring Boot 3 migration compilation issue */
        @SuppressWarnings("unchecked")
        List<Validator<T>> validators = (List<Validator<T>>) (List<?>) initValidators();

        return new IterableForEachValidator<>(createChildPath(), severity, errorMessage, validators, groups);
    }
}
