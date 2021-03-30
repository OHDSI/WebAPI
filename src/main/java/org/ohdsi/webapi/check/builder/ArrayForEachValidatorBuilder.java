package org.ohdsi.webapi.check.builder;

import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.validator.ValidatorGroup;
import org.ohdsi.webapi.check.validator.common.ArrayForEachValidator;

import java.util.List;

public class ArrayForEachValidatorBuilder<T> extends AbstractForEachValidatorBuilder<T, T[]> {
    @Override
    public Validator<T[]> build() {
        List<ValidatorGroup<T, ?>> groups = initGroups();
        List<Validator<T>> validators = initValidators();

        return new ArrayForEachValidator<>(createChildPath(), severity, errorMessage, validators, groups);
    }
}
