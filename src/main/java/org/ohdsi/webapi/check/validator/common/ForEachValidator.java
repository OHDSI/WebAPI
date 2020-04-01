package org.ohdsi.webapi.check.validator.common;

import com.google.common.collect.Lists;
import org.ohdsi.webapi.check.validator.Path;
import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.warning.WarningReporter;

import java.util.Collection;

public class ForEachValidator<T, V> extends Validator<Object> {
    private final Validator<T> validator;

    public ForEachValidator(Validator<T> validator) {
        this.validator = validator;
    }

    @Override
    public boolean validate(Object value) {
        Collection<T> values;
        if (value.getClass().isArray()) {
            values = Lists.<T>newArrayList((T[])value);
        } else if (value instanceof Collection){
            values = (Collection<T>)value;
        } else if (value instanceof Iterable){
            values = Lists.<T>newArrayList((Iterable)value);
        } else
            throw new RuntimeException("value must be of collection or array type");
        return values.stream()
                .map(validator::validate)
                .reduce(true, (left, right) -> left && right);
    }

    @Override
    public void build() {
        this.validator.build();
    }

    @Override
    public Validator setErrorTemplate(String errorTemplate) {
        this.validator.setErrorTemplate(errorTemplate);
        return super.setErrorTemplate(errorTemplate);
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
