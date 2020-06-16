package org.ohdsi.webapi.check.validator.common;

import com.google.common.collect.Lists;
import org.ohdsi.webapi.check.validator.Path;
import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.warning.WarningReporter;

import java.util.Collection;

public class IterableForEachValidator<T> extends Validator<Collection<? extends T>> {
    private Validator<T> validator;

    public IterableForEachValidator<T> setValidator(Validator<T> validator) {
        this.validator = validator;
        return this;
    }

    @Override
    public boolean validate(Collection<? extends T> value) {
        return Lists.newArrayList(value).stream()
                .allMatch(validator::validate);
    }

    @Override
    public void build() {
        this.validator.build();
    }

    @Override
    public IterableForEachValidator<T> setErrorMessage(String errorMessage) {
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
