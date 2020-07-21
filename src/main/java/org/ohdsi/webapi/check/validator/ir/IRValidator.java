package org.ohdsi.webapi.check.validator.ir;

import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.check.validator.Rule;
import org.ohdsi.webapi.check.validator.RuleValidator;
import org.ohdsi.webapi.check.validator.common.NotNullNotEmptyValidator;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysisExpression;
import org.ohdsi.webapi.service.dto.IRAnalysisDTO;

import java.util.function.Function;

public class IRValidator<T extends IRAnalysisDTO> extends RuleValidator<T> {
    @Override
    protected void buildInternal() {
        // Analysis expression
        prepareAnalysisExpressionRule();
    }

    private void prepareAnalysisExpressionRule() {
        Function<T, IncidenceRateAnalysisExpression> valueGetter = t -> {
            try {
                return Utils.deserialize(t.getExpression(), IncidenceRateAnalysisExpression.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        NotNullNotEmptyValidator notNullNotEmptyValidator = new NotNullNotEmptyValidator();
        notNullNotEmptyValidator.setPath(createPath("expression"));

        Rule<T, IncidenceRateAnalysisExpression> rule = new Rule<T, IncidenceRateAnalysisExpression>()
                .setPath(createPath())
                .setReporter(reporter)
                .setValueGetter(valueGetter)
                .addValidator(notNullNotEmptyValidator)
                .addValidator(new IRAnalysisExpressionValidator<>());
        rules.add(rule);
    }
}
