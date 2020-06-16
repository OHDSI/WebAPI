package org.ohdsi.webapi.check.validator.pathway;

import org.ohdsi.analysis.CohortMetadata;
import org.ohdsi.webapi.check.validator.Rule;
import org.ohdsi.webapi.check.validator.RuleValidator;
import org.ohdsi.webapi.check.validator.common.NotNullNotEmptyValidator;
import org.ohdsi.webapi.check.validator.common.PredicateValidator;
import org.ohdsi.webapi.pathway.dto.BasePathwayAnalysisDTO;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisDTO;

import java.util.List;

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
        Rule<T, Integer> rule = new Rule<T, Integer>()
                .setPath(createPath("maximum path length"))
                .setReporter(reporter)
                .setValueGetter(BasePathwayAnalysisDTO::getMaxDepth)
                .setDefaultErrorMessage("must be between 1 and 10")
                .addValidator(new NotNullNotEmptyValidator<>())
                .addValidator(new PredicateValidator<Integer>()
                        .setPredicate(v -> v >= 1 && v <= 10));
        rules.add(rule);
    }

    private void prepareCellCountWindowRule() {
        Rule<T, Integer> rule = new Rule<T, Integer>()
                .setPath(createPath("minimum cell count"))
                .setReporter(reporter)
                .setValueGetter(BasePathwayAnalysisDTO::getMinCellCount)
                .setDefaultErrorMessage("must be greater or equal to 0")
                .addValidator(new NotNullNotEmptyValidator<>())
                .addValidator(new PredicateValidator<Integer>()
                        .setPredicate(v -> v >= 0));
        rules.add(rule);
    }

    private void prepareCombinationWindowRule() {
        Rule<T, Integer> rule = new Rule<T, Integer>()
                .setPath(createPath("combination window"))
                .setReporter(reporter)
                .setValueGetter(BasePathwayAnalysisDTO::getCombinationWindow)
                .setDefaultErrorMessage("must be greater or equal to 0")
                .addValidator(new NotNullNotEmptyValidator<>())
                .addValidator(new PredicateValidator<Integer>()
                        .setPredicate(v -> v >= 0));
        rules.add(rule);
    }

    private void prepareEventCohortsRule() {
        Rule<T, List<? extends CohortMetadata>> rule = new Rule<T, List<? extends CohortMetadata>>()
                .setPath(createPath("event cohorts"))
                .setReporter(reporter)
                .setValueGetter(BasePathwayAnalysisDTO::getEventCohorts)
                .addValidator(new NotNullNotEmptyValidator<>());
        rules.add(rule);
    }

    private void prepareTargetCohortsRule() {
        Rule<T, List<? extends CohortMetadata>> rule = new Rule<T, List<? extends CohortMetadata>>()
                .setPath(createPath("target cohorts"))
                .setReporter(reporter)
                .setValueGetter(BasePathwayAnalysisDTO::getTargetCohorts)
                .addValidator(new NotNullNotEmptyValidator<>());
        rules.add(rule);
    }
}
