package org.ohdsi.webapi.check.validator.prediction;

import org.ohdsi.analysis.prediction.design.RunPlpArgs;
import org.ohdsi.webapi.check.validator.Rule;
import org.ohdsi.webapi.check.validator.RuleValidator;
import org.ohdsi.webapi.check.validator.common.NotNullNotEmptyValidator;
import org.ohdsi.webapi.check.validator.common.PredicateValidator;

public class RunPlpArgsValidator<T extends RunPlpArgs> extends RuleValidator<T> {
    @Override
    protected void buildInternal() {
        // Minimum covariate fraction
        prepareMinCovariateFractionRule();

        // Test fraction
        prepareTestFractionRule();
    }

    private void prepareTestFractionRule() {
        Rule<T, Float> rule = new Rule<T, Float>()
                .setPath(createPath("test fraction"))
                .setReporter(reporter)
                .setValueGetter(RunPlpArgs::getTestFraction)
                .setErrorMessage("must be between 0 and 100")
                .addValidator(new NotNullNotEmptyValidator<>())
                .addValidator(new PredicateValidator<Float>()
                        .setPredicate(v -> v >= 0.0 && v <= 1.0));
        rules.add(rule);
    }

    private void prepareMinCovariateFractionRule() {
        Rule<T, Float> rule = new Rule<T, Float>()
                .setPath(createPath("minimum covariate fraction"))
                .setReporter(reporter)
                .setValueGetter(RunPlpArgs::getMinCovariateFraction)
                .setErrorMessage("must be greater or equal to 0")
                .addValidator(new NotNullNotEmptyValidator<>())
                .addValidator(new PredicateValidator<Float>()
                        .setPredicate(v -> v >= 0.0));
        rules.add(rule);
    }
}