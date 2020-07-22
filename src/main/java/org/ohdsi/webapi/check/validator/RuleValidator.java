package org.ohdsi.webapi.check.validator;

import java.util.ArrayList;
import java.util.List;

public abstract class RuleValidator<T> extends Validator<T> {
    protected List<Rule<T, ?>> rules = new ArrayList<>();

    @Override
    public boolean validate(T value) {
        if (value == null) {
            return true;
        }
        return rules.stream()
                .map(r -> r.validate(value))
                .reduce(true, (left, right) -> left && right);
    }

    @Override
    public void configure() {
        buildInternal();
        rules.forEach(Rule::configure);
    }

    protected abstract void buildInternal();
}
