package org.ohdsi.webapi.check.builder;

import org.ohdsi.webapi.check.validator.Validator;

public abstract class ValidatorBuilder<T> extends ValidatorBaseBuilder<T, Validator<T>, ValidatorBuilder<T>> {

    public abstract Validator<T> build();

}
