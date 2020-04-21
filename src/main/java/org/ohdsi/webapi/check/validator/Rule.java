package org.ohdsi.webapi.check.validator;

import org.ohdsi.webapi.check.validator.common.NotNullNotEmptyValidator;
import org.ohdsi.webapi.check.warning.WarningReporter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Rule<T> {
    private List<Validator> validators = new ArrayList<>();
    private ValueAccessor<T> valueAccessor = (t) -> t;
    private String errorTemplate;
    protected Path path;
    protected WarningReporter reporter;

    public Rule(Path path, WarningReporter reporter) {
        this.path = path;
        this.reporter = reporter;
    }

    public Rule<T> setValueAccessor(ValueAccessor<T> valueAccessor) {
        this.valueAccessor = valueAccessor;
        return this;
    }

    public Rule<T> setErrorTemplate(String errorTemplate) {
        // Check validator errorTemplate and set it to new errorTemplate
        this.validators.forEach(v -> {
            // Do not change the error template if it is already set
            if (v.getDefaultErrorTemplate().equals(v.getErrorTemplate())) {
                v.setErrorTemplate(errorTemplate);
            }
        });
        this.errorTemplate = errorTemplate;
        return this;
    }

    public Rule<T> addValidator(Validator validator) {
        if ((validator.getDefaultErrorTemplate().equals(validator.getErrorTemplate()))
                && Objects.nonNull(this.errorTemplate)) {
            validator.setErrorTemplate(this.errorTemplate);
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
                    Object valueToValidate = valueAccessor.get(value);
                    boolean isValid = true;
                    if (valueToValidate != null ||
                            NotNullNotEmptyValidator.class.isInstance(v)) {
                        isValid = v.validate(valueToValidate);
                    }
                    return isValid;
                })
                .reduce(true, (left, right) -> left && right);
    }

    public Rule<T> build() {
        this.validators.forEach(Validator::build);
        return this;
    }
}
