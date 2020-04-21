package org.ohdsi.webapi.check.validator.estimation;

import org.ohdsi.analysis.estimation.design.PositiveControlSynthesisArgs;
import org.ohdsi.webapi.check.validator.Rule;
import org.ohdsi.webapi.check.validator.RuleValidator;
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
        Rule<T> rule =
                createRuleWithDefaultValidator(createPath("minimum number of outcome events required to inject a signal"), reporter)
                        .setErrorTemplate("must be greater or equal to 0")
                        .setValueAccessor(PositiveControlSynthesisArgs::getMinOutcomeCountForInjection)
                        .addValidator(new PredicateValidator<Integer>()
                                .setPredicate(v -> v >= 0));
        rules.add(rule);
    }

    private void prepareMinOutcomeCountForModelRule() {
        Rule<T> rule =
                createRuleWithDefaultValidator(createPath("minimum number of outcome events required to build a model"), reporter)
                        .setErrorTemplate("must be greater or equal to 0")
                        .setValueAccessor(PositiveControlSynthesisArgs::getMinOutcomeCountForModel)
                        .addValidator(new PredicateValidator<Integer>()
                                .setPredicate(v -> v >= 0));
        rules.add(rule);
    }

    private void prepareOutcomeIdOffsetRule() {
        Rule<T> rule =
                createRuleWithDefaultValidator(createPath("first new outcome ID that is to be created"), reporter)
                        .setErrorTemplate("must be greater or equal to 0")
                        .setValueAccessor(PositiveControlSynthesisArgs::getOutputIdOffset)
                        .addValidator(new PredicateValidator<Integer>()
                                .setPredicate(v -> v >= 0));
        rules.add(rule);
    }

    private void prepareRatioBetweenTargetAndInjectedRule() {
        Rule<T> rule =
                createRuleWithDefaultValidator(createPath("allowed ratio between target and injected signal size"), reporter)
                        .setErrorTemplate("must be greater or equal to 0")
                        .setValueAccessor(PositiveControlSynthesisArgs::getPrecision)
                        .addValidator(new PredicateValidator<Float>()
                                .setPredicate(v -> v >= 0));
        rules.add(rule);
    }

    private void prepareMaxPeopleFitModelRule() {
        Rule<T> rule =
                createRuleWithDefaultValidator(createPath("maximum number of people used to fit an outcome model"), reporter)
                        .setErrorTemplate("must be greater or equal to 0")
                        .setValueAccessor(PositiveControlSynthesisArgs::getMaxSubjectsForModel)
                        .addValidator(new PredicateValidator<Integer>()
                                .setPredicate(v -> v >= 0));
        rules.add(rule);
    }

    private void prepareMinRequiredObservationRule() {
        Rule<T> rule =
                createRuleWithDefaultValidator(createPath("minimum required continuous observation time"), reporter)
                        .setErrorTemplate("must be greater or equal to 0")
                        .setValueAccessor(PositiveControlSynthesisArgs::getWashoutPeriod)
                        .addValidator(new PredicateValidator<Integer>()
                                .setPredicate(v -> v >= 0));
        rules.add(rule);
    }

    private void prepareWindowEndRule() {
        Rule<T> rule =
                createRuleWithDefaultValidator(createPath("time-at-risk window end"), reporter)
                        .setErrorTemplate("must be greater or equal to 0")
                        .setValueAccessor(PositiveControlSynthesisArgs::getRiskWindowEnd)
                        .addValidator(new PredicateValidator<Integer>()
                                .setPredicate(v -> v >= 0));
        rules.add(rule);
    }

    private void prepareWindowStartRule() {
        Rule<T> rule =
                createRuleWithDefaultValidator(createPath("time-at-risk window start"), reporter)
                        .setErrorTemplate("must be greater or equal to 0")
                        .setValueAccessor(PositiveControlSynthesisArgs::getRiskWindowStart)
                        .addValidator(new PredicateValidator<Integer>()
                                .setPredicate(v -> v >= 0));
        rules.add(rule);
    }
}
