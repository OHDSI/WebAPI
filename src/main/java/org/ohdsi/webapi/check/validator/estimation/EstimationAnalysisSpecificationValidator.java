
package org.ohdsi.webapi.check.validator.estimation;

import org.ohdsi.analysis.Utils;
import org.ohdsi.analysis.estimation.comparativecohortanalysis.design.CohortMethodAnalysis;
import org.ohdsi.analysis.estimation.comparativecohortanalysis.design.ComparativeCohortAnalysis;
import org.ohdsi.analysis.estimation.comparativecohortanalysis.design.TargetComparatorOutcomes;
import org.ohdsi.webapi.check.validator.Rule;
import org.ohdsi.webapi.check.validator.RuleValidator;
import org.ohdsi.webapi.check.validator.ValueGetter;
import org.ohdsi.webapi.check.validator.common.DuplicateValidator;
import org.ohdsi.webapi.check.validator.common.ForEachValidator;
import org.ohdsi.webapi.check.validator.common.PredicateValidator;
import org.ohdsi.webapi.estimation.comparativecohortanalysis.specification.CohortMethodAnalysisImpl;

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

        Rule<T> rule =
                createRuleWithDefaultValidator(createPath("target comparator outcome"), reporter)
                        .setValueGetter(ComparativeCohortAnalysis::getTargetComparatorOutcomes)
                        .addValidator(new ForEachValidator<TargetComparatorOutcomes>()
                                .setValidator(tcoValidator))
                        .addValidator(new DuplicateValidator<TargetComparatorOutcomes>()
                                .setElementGetter(t -> t.getTargetId() + "," + t.getComparatorId()));
        rules.add(rule);
    }

    private void prepareAnalysisSettingsRule() {
        ValueGetter<CohortMethodAnalysis> elementGetter = value -> {
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

        Rule<T> rule =
                createRuleWithDefaultValidator(createPath("analysis settings"), reporter)
                        .setValueGetter(ComparativeCohortAnalysis::getCohortMethodAnalysisList)
                        .addValidator(new DuplicateValidator<CohortMethodAnalysis>()
                                .setElementGetter(elementGetter));
        rules.add(rule);
    }
}
