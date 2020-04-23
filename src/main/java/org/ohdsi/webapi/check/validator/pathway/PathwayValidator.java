package org.ohdsi.webapi.check.validator.pathway;

import org.ohdsi.webapi.check.validator.Rule;
import org.ohdsi.webapi.check.validator.RuleValidator;
import org.ohdsi.webapi.check.validator.common.PredicateValidator;
import org.ohdsi.webapi.pathway.dto.BasePathwayAnalysisDTO;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisDTO;

public class PathwayValidator<T extends PathwayAnalysisDTO> extends RuleValidator<T> {
    @Override
    protected void buildInternal() {
        // Target cohorts
        prepareTargetCohortsRule();

        // Event cohorts
        prepareEventCohortsRule();

        // Combination window
        prepareCombinationWindowRule();

        // Cell count window
        prepareCellCountWindowRule();

        // Maximum path length
        prepareMaxPathLengthRule();
    }

    private void prepareMaxPathLengthRule() {
        Rule<T> rule = createRuleWithDefaultValidator(createPath("maximum path length"), reporter)
                .setValueGetter(BasePathwayAnalysisDTO::getMaxDepth)
                .setErrorMessage("must be between 1 and 10")
                .addValidator(new PredicateValidator<Integer>()
                        .setPredicate(v -> v >= 1 && v <= 10));
        rules.add(rule);
    }

    private void prepareCellCountWindowRule() {
        Rule<T> rule = createRuleWithDefaultValidator(createPath("minimum cell count"), reporter)
                .setValueGetter(BasePathwayAnalysisDTO::getMinCellCount)
                .setErrorMessage("must be greater or equal to 0")
                .addValidator(new PredicateValidator<Integer>()
                        .setPredicate(v -> v >= 0));
        rules.add(rule);
    }

    private void prepareCombinationWindowRule() {
        Rule<T> rule = createRuleWithDefaultValidator(createPath("combination window"), reporter)
                .setValueGetter(BasePathwayAnalysisDTO::getCombinationWindow)
                .setErrorMessage("must be greater or equal to 0")
                .addValidator(new PredicateValidator<Integer>()
                        .setPredicate(v -> v >= 0));
        rules.add(rule);
    }

    private void prepareEventCohortsRule() {
        Rule<T> rule = createRuleWithDefaultValidator(createPath("event cohorts"), reporter)
                .setValueGetter(BasePathwayAnalysisDTO::getEventCohorts);
        rules.add(rule);
    }

    private void prepareTargetCohortsRule() {
        Rule<T> rule = createRuleWithDefaultValidator(createPath("target cohorts"), reporter)
                .setValueGetter(BasePathwayAnalysisDTO::getTargetCohorts);
        rules.add(rule);
    }
}
