package org.ohdsi.webapi.check.validator.estimation;

import org.ohdsi.analysis.ConceptSetCrossReference;
import org.ohdsi.analysis.estimation.design.EstimationAnalysis;
import org.ohdsi.analysis.estimation.design.EstimationAnalysisSettings;
import org.ohdsi.analysis.estimation.design.NegativeControlOutcomeCohortExpression;
import org.ohdsi.analysis.estimation.design.PositiveControlSynthesisArgs;
import org.ohdsi.webapi.check.validator.Rule;
import org.ohdsi.webapi.check.validator.RuleValidator;
import org.ohdsi.webapi.check.validator.ValueGetter;
import org.ohdsi.webapi.check.validator.common.NotNullNotEmptyValidator;
import org.ohdsi.webapi.check.validator.common.PredicateValidator;

import java.util.Collection;

import static org.ohdsi.webapi.estimation.EstimationServiceImpl.CONCEPT_SET_XREF_KEY_NEGATIVE_CONTROL_OUTCOMES;

public class EstimationSpecificationValidator<T extends EstimationAnalysis> extends RuleValidator<T> {
    @Override
    protected void buildInternal() {
        // Analysis settings
        prepareAnalysisSettingsRule();

        // Positive control synthesis
        preparePositiveSynthesisRule();

        // Negative control outcome
        prepareNegativeControlOutcomeRule();

        // Negative control
        prepareNegativeControlRule();
    }

    private void prepareNegativeControlRule() {
        PredicateValidator<Collection<? extends ConceptSetCrossReference>> validator = new PredicateValidator<>();
        validator.setPredicate(v -> {
            if (v != null) {
                return v.stream()
                        .anyMatch(r -> CONCEPT_SET_XREF_KEY_NEGATIVE_CONTROL_OUTCOMES.equalsIgnoreCase(r.getTargetName()));
            }
            return false;
        });
        Rule<T, Collection<? extends ConceptSetCrossReference>> rule = new Rule<T, Collection<? extends ConceptSetCrossReference>>()
                .setPath(createPath("negative control"))
                .setReporter(reporter)
                .setValueGetter(EstimationAnalysis::getConceptSetCrossReference)
                .addValidator(validator);
        rules.add(rule);
    }

    private void prepareNegativeControlOutcomeRule() {
        Rule<T, NegativeControlOutcomeCohortExpression> rule = new Rule<T, NegativeControlOutcomeCohortExpression>()
                .setPath(createPath("negative control outcome"))
                .setReporter(reporter)
                .setValueGetter(EstimationAnalysis::getNegativeControlOutcomeCohortDefinition)
                .addValidator(new NegativeControlOutcomeCohortExpressionValidator<>());
        rules.add(rule);
    }

    private void preparePositiveSynthesisRule() {
        ValueGetter<T, PositiveControlSynthesisArgs> positiveControlValueGetter =
                value -> Boolean.TRUE.equals(value.getDoPositiveControlSynthesis()) ?
                        value.getPositiveControlSynthesisArgs() :
                        null;

        Rule<T, PositiveControlSynthesisArgs> rule = new Rule<T, PositiveControlSynthesisArgs>()
                .setPath(createPath("positive control synthesis"))
                .setReporter(reporter)
                .setValueGetter(positiveControlValueGetter)
                .addValidator(new PositiveControlSynthesisArgsValidator<>());
        rules.add(rule);
    }

    private void prepareAnalysisSettingsRule() {
        Rule<T, EstimationAnalysisSettings> rule = new Rule<T, EstimationAnalysisSettings>()
                .setPath(createPath())
                .setReporter(reporter)
                .setValueGetter(EstimationAnalysis::getEstimationAnalysisSettings)
                .addValidator(new NotNullNotEmptyValidator<>())
                .addValidator(new EstimationSettingsValidator<>());
        rules.add(rule);
    }
}
