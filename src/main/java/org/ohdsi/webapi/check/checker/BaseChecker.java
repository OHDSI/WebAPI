package org.ohdsi.webapi.check.checker;

import org.ohdsi.webapi.check.Checker;
import org.ohdsi.webapi.check.validator.Path;
import org.ohdsi.webapi.check.validator.Rule;
import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.warning.DefaultWarning;
import org.ohdsi.webapi.check.warning.Warning;
import org.ohdsi.webapi.check.warning.WarningReporter;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseChecker<T> implements Checker<T> {
    public final List<Warning> check(T value) {
        List<Warning> warnings = new ArrayList();

        Rule<T> rule = createRule(getName(value), getReporter(warnings), getValidator());
        rule.validate(value);

        return warnings;
    }

    protected abstract String getName(T value);

    protected abstract Validator<T> getValidator();

    protected Rule<T> createRule(String name, WarningReporter reporter, Validator<T> validator) {
        return new Rule<T>(Path.createPath(name), reporter)
                .addValidator(validator)
                .build();
    }

    protected WarningReporter getReporter(List<Warning> warnings) {
        return (severity, template, params) -> {
            warnings.add(new DefaultWarning(severity, String.format(template, params)));
        };
    }
}