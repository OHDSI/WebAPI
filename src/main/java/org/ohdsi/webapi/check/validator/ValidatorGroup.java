package org.ohdsi.webapi.check.validator;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class ValidatorGroup<T, V> {
    private List<Validator<V>> validators;
    private List<ValidatorGroup<V, ?>> validatorGroups;
    private Function<T, V> valueForValidationGetter;
    private Function<T, Boolean> conditionGetter = t -> true;

    public ValidatorGroup(List<Validator<V>> validators,
                          List<ValidatorGroup<V, ?>> validatorGroups,
                          Function<T, V> valueForValidationGetter,
                          Function<T, Boolean> conditionGetter) {

        this.validators = validators;
        this.validatorGroups = validatorGroups;
        this.valueForValidationGetter = valueForValidationGetter;
        this.conditionGetter = conditionGetter;
    }

    public boolean validate(T value, Context context) {

        if (Objects.isNull(value)) {
            return true;
        }
        Boolean validatorsResult = this.validators.stream()
                .filter(v -> conditionGetter.apply(value))
                .map(v -> v.validate(valueForValidationGetter.apply(value), context))
                .reduce(true, (left, right) -> left && right);

        Boolean groupsResult = this.validatorGroups.stream()
                .filter(v -> conditionGetter.apply(value))
                .map(v -> v.validate(valueForValidationGetter.apply(value), context))
                .reduce(true, (left, right) -> left && right);
        return validatorsResult && groupsResult;
    }


}
