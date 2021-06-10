package org.ohdsi.webapi.check.checker.estimation.helper;

import static org.ohdsi.webapi.check.checker.estimation.helper.EstimationSettingsHelper.*;
import static org.ohdsi.webapi.check.checker.estimation.helper.NegativeControlOutcomeCohortExpressionHelper.*;
import static org.ohdsi.webapi.check.checker.estimation.helper.PositiveControlSynthesisArgsHelper.*;

import java.util.Collection;
import org.ohdsi.analysis.ConceptSetCrossReference;
import org.ohdsi.analysis.estimation.design.EstimationAnalysis;
import org.ohdsi.analysis.estimation.design.EstimationAnalysisSettings;
import org.ohdsi.analysis.estimation.design.NegativeControlOutcomeCohortExpression;
import org.ohdsi.analysis.estimation.design.PositiveControlSynthesisArgs;
import org.ohdsi.webapi.check.builder.NotNullNotEmptyValidatorBuilder;
import org.ohdsi.webapi.check.builder.PredicateValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;
import org.ohdsi.webapi.estimation.EstimationServiceImpl;

public class EstimationSpecificationHelper {

    public static ValidatorGroupBuilder<EstimationAnalysis, ?> prepareNegativeControlBuilder() {

        PredicateValidatorBuilder<Collection<? extends ConceptSetCrossReference>> validatorBuilder = new PredicateValidatorBuilder<>();
        validatorBuilder.predicate(v -> {
            if (v != null) {
                return v.stream()
                        .anyMatch(r -> EstimationServiceImpl.CONCEPT_SET_XREF_KEY_NEGATIVE_CONTROL_OUTCOMES.equalsIgnoreCase(r.getTargetName()));
            }
            return false;
        });
        ValidatorGroupBuilder<EstimationAnalysis, Collection<? extends ConceptSetCrossReference>> builder = new ValidatorGroupBuilder<EstimationAnalysis, Collection<? extends ConceptSetCrossReference>>()
                .attrName("negative control")
                .valueGetter(EstimationAnalysis::getConceptSetCrossReference)
                .validators(
                        validatorBuilder
                );
        return builder;


    }

    public static ValidatorGroupBuilder<EstimationAnalysis, NegativeControlOutcomeCohortExpression> prepareNegativeControlOutcomeBuilder() {

        ValidatorGroupBuilder<EstimationAnalysis, NegativeControlOutcomeCohortExpression> builder = new ValidatorGroupBuilder<EstimationAnalysis, NegativeControlOutcomeCohortExpression>()
                .attrName("negative control outcome")
                .valueGetter(EstimationAnalysis::getNegativeControlOutcomeCohortDefinition)
                .groups(
                        prepareOccurrenceTypeBuilder(),
                        prepareDetectOnDescendantsBuilder(),
                        prepareDomainsBuilder()
                );
        return builder;
    }

    public static ValidatorGroupBuilder<EstimationAnalysis, PositiveControlSynthesisArgs> preparePositiveSynthesisBuilder() {

        ValidatorGroupBuilder<EstimationAnalysis, PositiveControlSynthesisArgs> builder = new ValidatorGroupBuilder<EstimationAnalysis, PositiveControlSynthesisArgs>()
                .attrName("positive control synthesis")
                .valueGetter(value ->
                        Boolean.TRUE.equals(value.getDoPositiveControlSynthesis()) ? value.getPositiveControlSynthesisArgs() : null
                )
                .groups(
                        prepareWindowStartBuilder(),
                        prepareWindowEndBuilder(),
                        prepareMinRequiredObservationBuilder(),
                        prepareMaxPeopleFitModelBuilder(),
                        prepareRatioBetweenPositiveControlSynthesisArgsargetAndInjectedBuilder(),
                        prepareOutcomeIdOffsetBuilder(),
                        prepareMinOutcomeCountForModelBuilder(),
                        prepareMinOutcomeCountForInjectionBuilder()
                );
        return builder;
    }

    public static ValidatorGroupBuilder<EstimationAnalysis, EstimationAnalysisSettings> prepareAnalysisSettingsBuilder() {

        ValidatorGroupBuilder<EstimationAnalysis, EstimationAnalysisSettings> builder = new ValidatorGroupBuilder<EstimationAnalysis, EstimationAnalysisSettings>()
                .valueGetter(EstimationAnalysis::getEstimationAnalysisSettings)
                .validators(
                        new NotNullNotEmptyValidatorBuilder<>()
                )
                .groups(
                        prepareAnalysisSpecificationBuilder()
                );
        return builder;
    }
}
