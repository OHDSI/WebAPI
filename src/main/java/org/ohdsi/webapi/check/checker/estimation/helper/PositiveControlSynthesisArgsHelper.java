package org.ohdsi.webapi.check.checker.estimation.helper;

import org.ohdsi.analysis.estimation.design.PositiveControlSynthesisArgs;
import org.ohdsi.webapi.check.builder.NotNullNotEmptyValidatorBuilder;
import org.ohdsi.webapi.check.builder.PredicateValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;

public class PositiveControlSynthesisArgsHelper {

    public static ValidatorGroupBuilder<PositiveControlSynthesisArgs, Integer> prepareMinOutcomeCountForInjectionBuilder() {

        ValidatorGroupBuilder<PositiveControlSynthesisArgs, Integer> builder = new ValidatorGroupBuilder<PositiveControlSynthesisArgs, Integer>()
                .attrName("minimum number of outcome events required to inject a signal")
                .errorMessage("must be greater or equal to 0")
                .valueGetter(PositiveControlSynthesisArgs::getMinOutcomeCountForInjection)
                .validators(
                        new NotNullNotEmptyValidatorBuilder<>(),
                        new PredicateValidatorBuilder<Integer>()
                                .predicate(v -> v >= 0)
                );
        return builder;
    }

    public static ValidatorGroupBuilder<PositiveControlSynthesisArgs, Integer> prepareMinOutcomeCountForModelBuilder() {

        ValidatorGroupBuilder<PositiveControlSynthesisArgs, Integer> builder = new ValidatorGroupBuilder<PositiveControlSynthesisArgs, Integer>()
                .attrName("minimum number of outcome events required to build a model")
                .errorMessage("must be greater or equal to 0")
                .valueGetter(PositiveControlSynthesisArgs::getMinOutcomeCountForModel)
                .validators(
                        new NotNullNotEmptyValidatorBuilder<>(),
                        new PredicateValidatorBuilder<Integer>()
                                .predicate(v -> v >= 0)
                );
        return builder;
    }

    public static ValidatorGroupBuilder<PositiveControlSynthesisArgs, Integer> prepareOutcomeIdOffsetBuilder() {

        ValidatorGroupBuilder<PositiveControlSynthesisArgs, Integer> builder = new ValidatorGroupBuilder<PositiveControlSynthesisArgs, Integer>()
                .attrName("first new outcome ID that is to be created")
                .errorMessage("must be greater or equal to 0")
                .valueGetter(PositiveControlSynthesisArgs::getOutputIdOffset)
                .validators(
                        new NotNullNotEmptyValidatorBuilder<>(),
                        new PredicateValidatorBuilder<Integer>()
                                .predicate(v -> v >= 0)
                );
        return builder;
    }

    public static ValidatorGroupBuilder<PositiveControlSynthesisArgs, Float> prepareRatioBetweenPositiveControlSynthesisArgsargetAndInjectedBuilder() {

        ValidatorGroupBuilder<PositiveControlSynthesisArgs, Float> builder = new ValidatorGroupBuilder<PositiveControlSynthesisArgs, Float>()
                .attrName("allowed ratio between target and injected signal size")
                .errorMessage("must be greater or equal to 0")
                .valueGetter(PositiveControlSynthesisArgs::getPrecision)
                .validators(
                        new NotNullNotEmptyValidatorBuilder<>(),
                        new PredicateValidatorBuilder<Float>()
                                .predicate(v -> v >= 0)
                );
        return builder;
    }

    public static ValidatorGroupBuilder<PositiveControlSynthesisArgs, Integer> prepareMaxPeopleFitModelBuilder() {

        ValidatorGroupBuilder<PositiveControlSynthesisArgs, Integer> builder = new ValidatorGroupBuilder<PositiveControlSynthesisArgs, Integer>()
                .attrName("maximum number of people used to fit an outcome model")
                .errorMessage("must be greater or equal to 0")
                .valueGetter(PositiveControlSynthesisArgs::getMaxSubjectsForModel)
                .validators(
                        new NotNullNotEmptyValidatorBuilder<>(),
                        new PredicateValidatorBuilder<Integer>()
                                .predicate(v -> v >= 0)
                );
        return builder;
    }

    public static ValidatorGroupBuilder<PositiveControlSynthesisArgs, Integer> prepareMinRequiredObservationBuilder() {

        ValidatorGroupBuilder<PositiveControlSynthesisArgs, Integer> builder = new ValidatorGroupBuilder<PositiveControlSynthesisArgs, Integer>()
                .attrName("minimum required continuous observation time")
                .errorMessage("must be greater or equal to 0")
                .valueGetter(PositiveControlSynthesisArgs::getWashoutPeriod)
                .validators(
                        new NotNullNotEmptyValidatorBuilder<>(),
                        new PredicateValidatorBuilder<Integer>()
                                .predicate(v -> v >= 0)
                );
        return builder;
    }

    public static ValidatorGroupBuilder<PositiveControlSynthesisArgs, Integer> prepareWindowEndBuilder() {

        ValidatorGroupBuilder<PositiveControlSynthesisArgs, Integer> builder = new ValidatorGroupBuilder<PositiveControlSynthesisArgs, Integer>()
                .attrName("time-at-risk window end")
                .errorMessage("must be greater or equal to 0")
                .valueGetter(PositiveControlSynthesisArgs::getRiskWindowEnd)
                .validators(
                        new NotNullNotEmptyValidatorBuilder<>(),
                        new PredicateValidatorBuilder<Integer>()
                                .predicate(v -> v >= 0)
                );
        return builder;
    }

    public static ValidatorGroupBuilder<PositiveControlSynthesisArgs, Integer> prepareWindowStartBuilder() {

        ValidatorGroupBuilder<PositiveControlSynthesisArgs, Integer> builder = new ValidatorGroupBuilder<PositiveControlSynthesisArgs, Integer>()
                .attrName("time-at-risk window start")
                .errorMessage("must be greater or equal to 0")
                .valueGetter(PositiveControlSynthesisArgs::getRiskWindowStart)
                .validators(
                        new NotNullNotEmptyValidatorBuilder<>(),
                        new PredicateValidatorBuilder<Integer>()
                                .predicate(v -> v >= 0)
                );
        return builder;
    }
}
