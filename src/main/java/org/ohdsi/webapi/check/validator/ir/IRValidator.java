package org.ohdsi.webapi.check.validator.ir;

import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.check.validator.Rule;
import org.ohdsi.webapi.check.validator.RuleValidator;
import org.ohdsi.webapi.check.validator.ValueGetter;
import org.ohdsi.webapi.check.validator.common.NotNullNotEmptyValidator;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysisExpression;
import org.ohdsi.webapi.service.dto.IRAnalysisDTO;

public class IRValidator<T extends IRAnalysisDTO> extends RuleValidator<T> {
    @Override
    protected void buildInternal() {
        // Analysis expression
        prepareAnalysisExpressionRule();
    }

    private void prepareAnalysisExpressionRule() {
        ValueGetter<T, IncidenceRateAnalysisExpression> valueGetter = t -> {
            try {
                return Utils.deserialize(t.getExpression(), IncidenceRateAnalysisExpression.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        Rule<T, IncidenceRateAnalysisExpression> rule = new Rule<T, IncidenceRateAnalysisExpression>()
                .setPath(createPath())
                .setReporter(reporter)
                .setValueGetter(valueGetter)
                .addValidator(new NotNullNotEmptyValidator<>())
                .addValidator(new IRAnalysisExpressionValidator<>());
        rules.add(rule);
    }
}
