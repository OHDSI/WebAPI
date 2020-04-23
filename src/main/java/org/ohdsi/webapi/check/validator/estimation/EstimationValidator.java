package org.ohdsi.webapi.check.validator.estimation;

import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.check.validator.Rule;
import org.ohdsi.webapi.check.validator.RuleValidator;
import org.ohdsi.webapi.check.validator.ValueGetter;
import org.ohdsi.webapi.estimation.dto.EstimationDTO;
import org.ohdsi.webapi.estimation.specification.EstimationAnalysisImpl;

public class EstimationValidator<T extends EstimationDTO> extends RuleValidator<T> {
    @Override
    protected void buildInternal() {
        // Analysis expression
        prepareAnalysisExpressionRule();
    }

    private void prepareAnalysisExpressionRule() {
        ValueGetter<T> valueGetter = t -> {
            try {
                return Utils.deserialize(t.getSpecification(), EstimationAnalysisImpl.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        Rule<T> rule = createRuleWithDefaultValidator(createPath(), reporter)
                .setValueGetter(valueGetter)
                .addValidator(new EstimationSpecificationValidator());
        rules.add(rule);
    }
}
