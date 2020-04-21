package org.ohdsi.webapi.check.validator;

import org.ohdsi.webapi.check.validator.common.NotNullNotEmptyValidator;
import org.ohdsi.webapi.check.warning.WarningReporter;

import java.util.ArrayList;
import java.util.List;

public abstract class RuleValidator<T> extends Validator<T> {
    protected List<Rule<T>> rules = new ArrayList<>();

    protected Rule<T> createRuleWithDefaultValidator(Path path, WarningReporter reporter) {
        return new Rule<T>(path, reporter)
                .addValidator(new NotNullNotEmptyValidator());
    }

    protected Rule<T> createRule(Path path, WarningReporter reporter) {
        return new Rule<T>(path, reporter);
    }

    @Override
    public boolean validate(T value) {
        return rules.stream()
                .map(r -> r.validate(value))
                .reduce(true, (left, right) -> left && right);
    }

    @Override
    public void build() {
        buildInternal();
        rules.forEach(Rule::build);
    }

    protected abstract void buildInternal();
}
