package org.ohdsi.webapi.check.validator.estimation;

import org.ohdsi.analysis.Utils;
import org.ohdsi.analysis.estimation.design.EstimationAnalysis;
import org.ohdsi.webapi.check.validator.Rule;
import org.ohdsi.webapi.check.validator.RuleValidator;
import org.ohdsi.webapi.check.validator.common.NotNullNotEmptyValidator;
import org.ohdsi.webapi.estimation.dto.EstimationDTO;
import org.ohdsi.webapi.estimation.specification.EstimationAnalysisImpl;

import java.util.function.Function;

public class EstimationValidator<T extends EstimationDTO> extends RuleValidator<T> {
    @Override
    protected void buildInternal() {
        // Analysis expression
        prepareAnalysisExpressionRule();
    }

    private void prepareAnalysisExpressionRule() {
        Function<T, EstimationAnalysis> valueGetter = t -> {
            try {
                return Utils.deserialize(t.getSpecification(), EstimationAnalysisImpl.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        Rule<T, EstimationAnalysis> rule = new Rule<T, EstimationAnalysis>()
                .setPath(createPath("specification"))
                .setReporter(reporter)
                .setValueGetter(valueGetter)
                .addValidator(new NotNullNotEmptyValidator<>())
                .addValidator(new EstimationSpecificationValidator<>());
        rules.add(rule);
    }
}
