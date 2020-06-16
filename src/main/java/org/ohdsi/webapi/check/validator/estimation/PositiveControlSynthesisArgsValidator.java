package org.ohdsi.webapi.check.validator.estimation;

import org.ohdsi.analysis.estimation.design.PositiveControlSynthesisArgs;
import org.ohdsi.webapi.check.validator.Rule;
import org.ohdsi.webapi.check.validator.RuleValidator;
import org.ohdsi.webapi.check.validator.common.NotNullNotEmptyValidator;
import org.ohdsi.webapi.check.validator.common.PredicateValidator;

public class PositiveControlSynthesisArgsValidator<T extends PositiveControlSynthesisArgs> extends RuleValidator<T> {
    @Override
    protected void buildInternal() {
        // Time-at-risk window start
        prepareWindowStartRule();

        // Time-at-risk window end
        prepareWindowEndRule();

        // Minimum required continuous observation time
        prepareMinRequiredObservationRule();

        // Maximum number of people used to fit an outcome model
        prepareMaxPeopleFitModelRule();

        // Allowed ratio between target and injected signal size
        prepareRatioBetweenTargetAndInjectedRule();

        // First new outcome ID that is to be created
        prepareOutcomeIdOffsetRule();

        // Minimum number of outcome events required to build a model
        prepareMinOutcomeCountForModelRule();

        // Minimum number of outcome events required to inject a signal
        prepareMinOutcomeCountForInjectionRule();
    }

    private void prepareMinOutcomeCountForInjectionRule() {
        Rule<T, Integer> rule = new Rule<T, Integer>()
                .setPath(createPath("minimum number of outcome events required to inject a signal"))
                .setReporter(reporter)
                .setDefaultErrorMessage("must be greater or equal to 0")
                .setValueGetter(PositiveControlSynthesisArgs::getMinOutcomeCountForInjection)
                .addValidator(new NotNullNotEmptyValidator<>())
                .addValidator(new PredicateValidator<Integer>()
                        .setPredicate(v -> v >= 0));
        rules.add(rule);
    }

    private void prepareMinOutcomeCountForModelRule() {
        Rule<T, Integer> rule = new Rule<T, Integer>()
                .setPath(createPath("minimum number of outcome events required to build a model"))
                .setReporter(reporter)
                .setDefaultErrorMessage("must be greater or equal to 0")
                .setValueGetter(PositiveControlSynthesisArgs::getMinOutcomeCountForModel)
                .addValidator(new NotNullNotEmptyValidator<>())
                .addValidator(new PredicateValidator<Integer>()
                        .setPredicate(v -> v >= 0));
        rules.add(rule);
    }

    private void prepareOutcomeIdOffsetRule() {
        Rule<T, Integer> rule = new Rule<T, Integer>()
                .setPath(createPath("first new outcome ID that is to be created"))
                .setReporter(reporter)
                .setDefaultErrorMessage("must be greater or equal to 0")
                .setValueGetter(PositiveControlSynthesisArgs::getOutputIdOffset)
                .addValidator(new NotNullNotEmptyValidator<>())
                .addValidator(new PredicateValidator<Integer>()
                        .setPredicate(v -> v >= 0));
        rules.add(rule);
    }

    private void prepareRatioBetweenTargetAndInjectedRule() {
        Rule<T, Float> rule = new Rule<T, Float>()
                .setPath(createPath("allowed ratio between target and injected signal size"))
                .setReporter(reporter)
                .setDefaultErrorMessage("must be greater or equal to 0")
                .setValueGetter(PositiveControlSynthesisArgs::getPrecision)
                .addValidator(new NotNullNotEmptyValidator<>())
                .addValidator(new PredicateValidator<Float>()
                        .setPredicate(v -> v >= 0));
        rules.add(rule);
    }

    private void prepareMaxPeopleFitModelRule() {
        Rule<T, Integer> rule = new Rule<T, Integer>()
                .setPath(createPath("maximum number of people used to fit an outcome model"))
                .setReporter(reporter)
                .setDefaultErrorMessage("must be greater or equal to 0")
                .setValueGetter(PositiveControlSynthesisArgs::getMaxSubjectsForModel)
                .addValidator(new NotNullNotEmptyValidator<>())
                .addValidator(new PredicateValidator<Integer>()
                        .setPredicate(v -> v >= 0));
        rules.add(rule);
    }

    private void prepareMinRequiredObservationRule() {
        Rule<T, Integer> rule = new Rule<T, Integer>()
                .setPath(createPath("minimum required continuous observation time"))
                .setReporter(reporter)
                .setDefaultErrorMessage("must be greater or equal to 0")
                .setValueGetter(PositiveControlSynthesisArgs::getWashoutPeriod)
                .addValidator(new NotNullNotEmptyValidator<>())
                .addValidator(new PredicateValidator<Integer>()
                        .setPredicate(v -> v >= 0));
        rules.add(rule);
    }

    private void prepareWindowEndRule() {
        Rule<T, Integer> rule = new Rule<T, Integer>()
                .setPath(createPath("time-at-risk window end"))
                .setReporter(reporter)
                .setDefaultErrorMessage("must be greater or equal to 0")
                .setValueGetter(PositiveControlSynthesisArgs::getRiskWindowEnd)
                .addValidator(new NotNullNotEmptyValidator<>())
                .addValidator(new PredicateValidator<Integer>()
                        .setPredicate(v -> v >= 0));
        rules.add(rule);
    }

    private void prepareWindowStartRule() {
        Rule<T, Integer> rule = new Rule<T, Integer>()
                .setPath(createPath("time-at-risk window start"))
                .setReporter(reporter)
                .setDefaultErrorMessage("must be greater or equal to 0")
                .setValueGetter(PositiveControlSynthesisArgs::getRiskWindowStart)
                .addValidator(new NotNullNotEmptyValidator<>())
                .addValidator(new PredicateValidator<Integer>()
                        .setPredicate(v -> v >= 0));
        rules.add(rule);
    }
}
