package org.ohdsi.webapi.check.validator.common;

import com.google.common.collect.Lists;
import org.ohdsi.webapi.check.validator.Path;
import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.warning.WarningReporter;

import java.util.Collection;

public class ForEachValidator<T> extends Validator<Object> {
    private Validator<T> validator;

    public ForEachValidator<T> setValidator(Validator<T> validator) {
        this.validator = validator;
        return this;
    }

    @Override
    public boolean validate(Object value) {
        Collection<T> values;
        if (value.getClass().isArray()) {
            values = Lists.newArrayList((T[]) value);
        } else if (value instanceof Collection) {
            values = (Collection<T>) value;
        } else if (value instanceof Iterable) {
            values = Lists.newArrayList((Iterable<T>) value);
        } else
            throw new RuntimeException("value must be of collection, iterable or array type");
        return values.stream()
                .map(validator::validate)
                .reduce(true, (left, right) -> left && right);
    }

    @Override
    public void build() {
        this.validator.build();
    }

    @Override
    public ForEachValidator<T> setErrorMessage(String errorMessage) {
        this.validator.setErrorMessage(errorMessage);
        return this;
    }

    @Override
    public void setPath(Path path) {
        this.validator.setPath(path);
        super.setPath(path);
    }

    @Override
    public void setReporter(WarningReporter reporter) {
        this.validator.setReporter(reporter);
        super.setReporter(reporter);
    }
}
