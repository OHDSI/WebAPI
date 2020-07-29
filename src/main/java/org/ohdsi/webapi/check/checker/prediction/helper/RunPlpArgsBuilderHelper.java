package org.ohdsi.webapi.check.checker.prediction.helper;

import org.ohdsi.analysis.prediction.design.RunPlpArgs;
import org.ohdsi.webapi.check.builder.NotNullNotEmptyValidatorBuilder;
import org.ohdsi.webapi.check.builder.PredicateValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;

public class RunPlpArgsBuilderHelper {

    public static ValidatorGroupBuilder<RunPlpArgs, Float> prepareTestFractionBuilder() {

        ValidatorGroupBuilder<RunPlpArgs, Float> builder = new ValidatorGroupBuilder<RunPlpArgs, Float>()
                .attrName("test fraction")
                .valueGetter(RunPlpArgs::getTestFraction)
                .validators(
                        new NotNullNotEmptyValidatorBuilder<>(),
                        new PredicateValidatorBuilder<Float>()
                                .predicate(v -> v >= 0.0 && v <= 1.0)
                                .errorMessage("must be between 0 and 100")
                );
        return builder;
    }

    public static ValidatorGroupBuilder<RunPlpArgs, Float> prepareMinCovariateFractionBuilder() {

        ValidatorGroupBuilder<RunPlpArgs, Float> builder = new ValidatorGroupBuilder<RunPlpArgs, Float>()
                .attrName("minimum covariate fraction")
                .valueGetter(RunPlpArgs::getMinCovariateFraction)
                .validators(
                        new NotNullNotEmptyValidatorBuilder<>(),
                        new PredicateValidatorBuilder<Float>()
                                .predicate(v -> v >= 0.0)
                                .errorMessage("must be greater or equal to 0")
                );
        return builder;
    }
}
