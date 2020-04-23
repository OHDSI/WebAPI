package org.ohdsi.webapi.check.validator.ir;

import org.ohdsi.webapi.check.validator.Rule;
import org.ohdsi.webapi.check.validator.RuleValidator;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysisExpression;

public class IRAnalysisExpressionValidator<T extends IncidenceRateAnalysisExpression> extends RuleValidator<T> {
    @Override
    protected void buildInternal() {
        // Target cohorts
        prepareTargetCohortsRule();

        // Outcome cohorts
        prepareOutcomeCohortsRule();
    }

    private void prepareOutcomeCohortsRule() {
        Rule<T> rule = createRuleWithDefaultValidator(createPath("outcome cohorts"), reporter)
                .setValueGetter(t -> t.outcomeIds);
        rules.add(rule);
    }

    private void prepareTargetCohortsRule() {
        Rule<T> rule = createRuleWithDefaultValidator(createPath("target cohorts"), reporter)
                .setValueGetter(t -> t.targetIds);
        rules.add(rule);
    }
}
