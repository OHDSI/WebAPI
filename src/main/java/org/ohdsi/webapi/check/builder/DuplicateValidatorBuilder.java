package org.ohdsi.webapi.check.builder;

import java.util.Collection;
import java.util.function.Function;
import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.validator.common.DuplicateValidator;

public class DuplicateValidatorBuilder<T, V> extends PredicateValidatorBuilder<Collection<? extends T>> {

    private Function<T, V> elementGetter;

    public DuplicateValidatorBuilder<T, V> elementGetter(Function<T, V> elementGetter) {

        this.elementGetter = elementGetter;
        return this;
    }

    @Override
    public Validator<Collection<? extends T>> build() {

        return new DuplicateValidator<>(createChildPath(),  severity, errorMessage, predicate, elementGetter);
    }
}
