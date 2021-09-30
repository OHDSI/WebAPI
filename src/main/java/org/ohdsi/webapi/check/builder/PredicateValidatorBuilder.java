package org.ohdsi.webapi.check.builder;

import java.util.function.Function;
import java.util.function.Predicate;
import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.validator.common.PredicateValidator;

public class PredicateValidatorBuilder<T> extends ValidatorBuilder<T> {

    protected Predicate<T> predicate;

    public PredicateValidatorBuilder<T> predicate(Predicate<T> predicate) {

        this.predicate = predicate;
        return this;
    }

    @Override
    public Validator<T> build() {

        return new PredicateValidator<>(createChildPath(), severity, errorMessage, predicate, attrNameValueGetter);
    }

}
