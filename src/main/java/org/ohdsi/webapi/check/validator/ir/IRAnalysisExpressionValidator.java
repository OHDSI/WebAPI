package org.ohdsi.webapi.check.validator.ir;

import org.ohdsi.webapi.check.validator.Rule;
import org.ohdsi.webapi.check.validator.RuleValidator;
import org.ohdsi.webapi.check.validator.common.NotNullNotEmptyValidator;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysisExpression;

import java.util.List;

public class IRAnalysisExpressionValidator<T extends IncidenceRateAnalysisExpression> extends RuleValidator<T> {
    @Override
    protected void buildInternal() {
        // Target cohorts
        prepareTargetCohortsRule();

        // Outcome cohorts
        prepareOutcomeCohortsRule();
    }

    private void prepareOutcomeCohortsRule() {
        Rule<T, List<Integer>> rule = new Rule<T, List<Integer>>()
                .setPath(createPath("outcome cohorts"))
                .setReporter(reporter)
                .setValueGetter(t -> t.outcomeIds)
                .addValidator(new NotNullNotEmptyValidator<>());
        rules.add(rule);
    }

    private void prepareTargetCohortsRule() {
        Rule<T, List<Integer>> rule = new Rule<T, List<Integer>>()
                .setPath(createPath("target cohorts"))
                .setReporter(reporter)
                .setValueGetter(t -> t.targetIds)
                .addValidator(new NotNullNotEmptyValidator<>());
        rules.add(rule);
    }
}
