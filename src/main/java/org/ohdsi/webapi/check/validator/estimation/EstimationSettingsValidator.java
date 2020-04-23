package org.ohdsi.webapi.check.validator.estimation;

import org.ohdsi.analysis.estimation.comparativecohortanalysis.design.ComparativeCohortAnalysis;
import org.ohdsi.analysis.estimation.design.EstimationAnalysisSettings;
import org.ohdsi.webapi.check.validator.Rule;
import org.ohdsi.webapi.check.validator.RuleValidator;
import org.ohdsi.webapi.check.validator.common.NotNullNotEmptyValidator;

public class EstimationSettingsValidator<T extends EstimationAnalysisSettings> extends RuleValidator<T> {
    @Override
    protected void buildInternal() {
        // Analysis specification
        prepareAnalysisSpecificationRule();
    }

    private void prepareAnalysisSpecificationRule() {
        Rule<T, ComparativeCohortAnalysis> rule = new Rule<T, ComparativeCohortAnalysis>()
                .setPath(createPath())
                .setReporter(reporter)
                .setValueGetter(EstimationAnalysisSettings::getAnalysisSpecification)
                .addValidator(new NotNullNotEmptyValidator<>())
                .addValidator(new EstimationAnalysisSpecificationValidator<>());
        rules.add(rule);
    }
}
