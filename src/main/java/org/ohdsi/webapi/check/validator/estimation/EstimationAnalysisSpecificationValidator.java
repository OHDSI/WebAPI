package org.ohdsi.webapi.check.validator.estimation;

import org.ohdsi.analysis.Utils;
import org.ohdsi.analysis.estimation.comparativecohortanalysis.design.CohortMethodAnalysis;
import org.ohdsi.analysis.estimation.comparativecohortanalysis.design.ComparativeCohortAnalysis;
import org.ohdsi.analysis.estimation.comparativecohortanalysis.design.TargetComparatorOutcomes;
import org.ohdsi.webapi.check.validator.Rule;
import org.ohdsi.webapi.check.validator.RuleValidator;
import org.ohdsi.webapi.check.validator.common.DuplicateValidator;
import org.ohdsi.webapi.check.validator.common.IterableForEachValidator;
import org.ohdsi.webapi.check.validator.common.NotNullNotEmptyValidator;
import org.ohdsi.webapi.check.validator.common.PredicateValidator;
import org.ohdsi.webapi.estimation.comparativecohortanalysis.specification.CohortMethodAnalysisImpl;

import java.util.Collection;
import java.util.function.Function;

public class EstimationAnalysisSpecificationValidator<T extends ComparativeCohortAnalysis> extends RuleValidator<T> {
    @Override
    protected void buildInternal() {
        // Target comparator outcome
        prepareTargetComparatorRule();

        // Analysis settings
        prepareAnalysisSettingsRule();
    }

    private void prepareTargetComparatorRule() {
        PredicateValidator<TargetComparatorOutcomes> tcoValidator = new PredicateValidator<>();
        tcoValidator.setPredicate(t -> {
            if (t != null) {
                return t.getComparatorId() != null
                        && t.getTargetId() != null
                        && t.getOutcomeIds().size() > 0;
            }
            return false;
        });
        tcoValidator.setErrorMessage("no target, comparator or outcome");

        Rule<T, Collection<? extends TargetComparatorOutcomes>> rule = new Rule<T, Collection<? extends TargetComparatorOutcomes>>()
                .setPath(createPath("target comparator outcome"))
                .setReporter(reporter)
                .setValueGetter(ComparativeCohortAnalysis::getTargetComparatorOutcomes)
                .addValidator(new NotNullNotEmptyValidator<>())
                .addValidator(new IterableForEachValidator<TargetComparatorOutcomes>()
                        .setValidator(tcoValidator))
                .addValidator(new DuplicateValidator<TargetComparatorOutcomes, String>()
                        .setElementGetter(t -> t.getTargetId() + "," + t.getComparatorId()));
        rules.add(rule);
    }

    private void prepareAnalysisSettingsRule() {
        Function<CohortMethodAnalysis, String> elementGetter = value -> {
            CohortMethodAnalysisImpl analysis = ((CohortMethodAnalysisImpl) value);
            Integer analysisId = analysis.getAnalysisId();
            String description = analysis.getDescription();

            // remove identifier and description
            analysis.setAnalysisId(null);
            analysis.setDescription(null);

            String json = Utils.serialize(analysis);

            // restore identifier and description
            analysis.setAnalysisId(analysisId);
            analysis.setDescription(description);

            return json;
        };

        Rule<T, Collection<? extends CohortMethodAnalysis>> rule = new Rule<T, Collection<? extends CohortMethodAnalysis>>()
                .setPath(createPath("analysis settings"))
                .setReporter(reporter)
                .setValueGetter(ComparativeCohortAnalysis::getCohortMethodAnalysisList)
                .addValidator(new NotNullNotEmptyValidator<>())
                .addValidator(new DuplicateValidator<CohortMethodAnalysis, String>()
                        .setElementGetter(elementGetter));
        rules.add(rule);
    }
}
