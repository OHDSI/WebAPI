package org.ohdsi.webapi.check.validator;

import org.ohdsi.webapi.check.validator.common.NotNullNotEmptyValidator;
import org.ohdsi.webapi.check.warning.WarningReporter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Rule<T, V> {
    private final List<Validator<V>> validators = new ArrayList<>();
    private ValueGetter<T, V> valueGetter;
    private String errorMessage;
    private Path path;
    private WarningReporter reporter;

//    public Rule(Path path, WarningReporter reporter) {
//        this.path = path;
//        this.reporter = reporter;
//    }

    public Rule<T, V> setPath(Path path) {
        this.path = path;
        return this;
    }

    public Rule<T, V> setReporter(WarningReporter reporter) {
        this.reporter = reporter;
        return this;
    }

    public Rule<T, V> setValueGetter(ValueGetter<T, V> valueGetter) {
        this.valueGetter = valueGetter;
        return this;
    }

    public Rule<T, V> setErrorMessage(String errorMessage) {
        this.validators.forEach(v -> {
            // Do not change the error message of the validator if it is already changed
            if (v.isErrorMessageInitial()) {
                v.setErrorMessage(errorMessage);
            }
        });
        this.errorMessage = errorMessage;
        return this;
    }

    public Rule<T, V> addValidator(Validator<V> validator) {
        // if validator error message is not changed and rule message is set
        // change validator message to rule message
        if (!validator.isErrorMessageInitial() && Objects.nonNull(this.errorMessage)) {
            validator.setErrorMessage(this.errorMessage);
        }
        if (Objects.isNull(validator.getPath())) {
            validator.setPath(path);
        }
        if (Objects.isNull(validator.getReporter())) {
            validator.setReporter(reporter);
        }

        this.validators.add(validator);
        return this;
    }

    public boolean validate(T value) {
        return this.validators.stream()
                .map(v -> {
                    V valueToValidate = valueGetter.get(value);
                    boolean isValid = true;
                    if (valueToValidate != null ||
                            v instanceof NotNullNotEmptyValidator) {
                        isValid = v.validate(valueToValidate);
                    }
                    return isValid;
                })
                .reduce(true, (left, right) -> left && right);
    }

    public Rule<T, V> build() {
        this.validators.forEach(Validator::build);
        return this;
    }
}
