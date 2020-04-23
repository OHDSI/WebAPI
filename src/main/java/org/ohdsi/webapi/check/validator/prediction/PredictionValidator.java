package org.ohdsi.webapi.check.validator.prediction;

import org.ohdsi.analysis.Utils;
import org.ohdsi.analysis.prediction.design.PatientLevelPredictionAnalysis;
import org.ohdsi.webapi.check.validator.Rule;
import org.ohdsi.webapi.check.validator.RuleValidator;
import org.ohdsi.webapi.check.validator.ValueGetter;
import org.ohdsi.webapi.check.validator.common.NotNullNotEmptyValidator;
import org.ohdsi.webapi.prediction.dto.PredictionAnalysisDTO;
import org.ohdsi.webapi.prediction.specification.PatientLevelPredictionAnalysisImpl;

public class PredictionValidator<T extends PredictionAnalysisDTO> extends RuleValidator<T> {
    @Override
    protected void buildInternal() {
        // Analysis expression
        prepareAnalysisExpressionRule();
    }

    private void prepareAnalysisExpressionRule() {
        ValueGetter<T, PatientLevelPredictionAnalysis> valueGetter = t -> {
            try {
                return Utils.deserialize(t.getSpecification(), PatientLevelPredictionAnalysisImpl.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        Rule<T, PatientLevelPredictionAnalysis> rule = new Rule<T, PatientLevelPredictionAnalysis>()
                .setPath(createPath("specification"))
                .setReporter(reporter)
                .setValueGetter(valueGetter)
                .addValidator(new NotNullNotEmptyValidator<>())
                .addValidator(new PredictionSpecificationValidator<>());
        rules.add(rule);
    }
}
