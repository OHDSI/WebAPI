package org.ohdsi.webapi.check.validator.estimation;

import org.ohdsi.analysis.estimation.design.EstimationAnalysisSettings;
import org.ohdsi.webapi.check.validator.Rule;
import org.ohdsi.webapi.check.validator.RuleValidator;

public class EstimationSettingsValidator<T extends EstimationAnalysisSettings> extends RuleValidator<T> {
    @Override
    protected void buildInternal() {
        // Analysis specification
        prepareAnalysisSpecificationRule();
    }

    private void prepareAnalysisSpecificationRule() {
        Rule<T> rule =
                createRuleWithDefaultValidator(createPath(), reporter)
                        .setValueGetter(EstimationAnalysisSettings::getAnalysisSpecification)
                        .addValidator(new EstimationAnalysisSpecificationValidator());
        rules.add(rule);
    }
}
