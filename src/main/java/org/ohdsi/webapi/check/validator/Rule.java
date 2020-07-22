package org.ohdsi.webapi.check.validator;

import org.ohdsi.webapi.check.validator.common.NotNullNotEmptyValidator;
import org.ohdsi.webapi.check.warning.WarningReporter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class Rule<T, V> {
    private final List<Validator<V>> validators = new ArrayList<>();
    // Function for getting value for validate
    private Function<T, V> valueGetter;
    private String errorMessage;
    private Path path;
    // Function for creating warning message based on preset parameters
    private WarningReporter reporter;

    public Rule<T, V> addValidator(Validator<V> validator) {
        this.validators.add(validator);
        return this;
    }

    public boolean validate(T value) {
        return this.validators.stream()
                .map(v -> v.validate(valueGetter.apply(value)))
                .reduce(true, (left, right) -> left && right);
    }

    public Rule<T, V> configure() {
        this.validators.forEach(v -> {
            // if validator error message is not changed and rule message is set
            // change validator message to rule message
            if (!v.isErrorMessageInitial() && Objects.nonNull(this.errorMessage)) {
                v.setErrorMessage(this.errorMessage);
            }
            if (Objects.isNull(v.getPath())) {
                v.setPath(path);
            }
            if (Objects.isNull(v.getReporter())) {
                v.setReporter(reporter);
            }

            v.configure();
        });
        return this;
    }

    public Rule<T, V> setPath(Path path) {
        this.path = path;
        return this;
    }

    public Rule<T, V> setReporter(WarningReporter reporter) {
        this.reporter = reporter;
        return this;
    }

    public Rule<T, V> setValueGetter(Function<T, V> valueGetter) {
        this.valueGetter = valueGetter;
        return this;
    }

    public Rule<T, V> setDefaultErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }
}
