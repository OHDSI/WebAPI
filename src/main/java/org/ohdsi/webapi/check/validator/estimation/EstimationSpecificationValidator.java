package org.ohdsi.webapi.check.validator.estimation;

import org.ohdsi.analysis.ConceptSetCrossReference;
import org.ohdsi.analysis.estimation.design.EstimationAnalysis;
import org.ohdsi.webapi.check.validator.Rule;
import org.ohdsi.webapi.check.validator.RuleValidator;
import org.ohdsi.webapi.check.validator.ValueGetter;
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
        Rule<T> rule =
                createRule(createPath("negative control"), reporter)
                        .setErrorMessage("must be present")
                        .setValueGetter(EstimationAnalysis::getConceptSetCrossReference)
                        .addValidator(validator);
        rules.add(rule);
    }

    private void prepareNegativeControlOutcomeRule() {
        Rule<T> rule =
                createRule(createPath("negative control outcome"), reporter)
                        .setValueGetter(EstimationAnalysis::getNegativeControlOutcomeCohortDefinition)
                        .addValidator(new NegativeControlOutcomeCohortExpressionValidator());
        rules.add(rule);
    }

    private void preparePositiveSynthesisRule() {
        ValueGetter<T> positiveControlValueGetter =
                value -> Boolean.TRUE.equals(value.getDoPositiveControlSynthesis()) ?
                        value.getPositiveControlSynthesisArgs() :
                        null;

        Rule<T> rule =
                createRule(createPath("positive control synthesis"), reporter)
                        .setValueGetter(positiveControlValueGetter)
                        .addValidator(new PositiveControlSynthesisArgsValidator());
        rules.add(rule);
    }

    private void prepareAnalysisSettingsRule() {
        Rule<T> rule =
                createRuleWithDefaultValidator(createPath(), reporter)
                        .setValueGetter(EstimationAnalysis::getEstimationAnalysisSettings)
                        .addValidator(new EstimationSettingsValidator());
        rules.add(rule);
    }
}
