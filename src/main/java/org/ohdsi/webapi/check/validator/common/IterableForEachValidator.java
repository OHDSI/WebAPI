package org.ohdsi.webapi.check.validator.common;

import org.ohdsi.webapi.check.validator.Context;
import org.ohdsi.webapi.check.validator.Path;
import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.validator.ValidatorGroup;
import org.ohdsi.webapi.check.warning.WarningSeverity;

import java.util.Collection;
import java.util.List;

public class IterableForEachValidator<T> extends Validator<Collection<? extends T>> {
    private final List<Validator<T>> validators;
    private final List<ValidatorGroup<T, ?>> validatorGroups;

    public IterableForEachValidator(Path path,
                                    WarningSeverity severity,
                                    String errorMessage,
                                    List<Validator<T>> validators,
                                    List<ValidatorGroup<T, ?>> validatorGroups) {

        super(path, severity, errorMessage);
        this.validators = validators;
        this.validatorGroups = validatorGroups;
    }

    @Override
    public boolean validate(Collection<? extends T> value, Context context) {

        if (value == null) {
            return true;
        }

        return value.stream()
                .map(item -> {
                    Boolean validatorsResult = this.validators.stream()
                            .map(v -> v.validate(item, context))
                            .reduce(true, (left, right) -> left && right);

                    Boolean groupsResult = this.validatorGroups.stream()
                            .map(v -> v.validate(item, context))
                            .reduce(true, (left, right) -> left && right);
                    return validatorsResult && groupsResult;
                })
                .reduce(true, (left, right) -> left && right);
    }

}
