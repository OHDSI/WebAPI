package org.ohdsi.webapi.check.checker.prediction;

import org.ohdsi.webapi.check.checker.BaseChecker;
import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.validator.prediction.PredictionValidator;
import org.ohdsi.webapi.prediction.dto.PredictionAnalysisDTO;

public class PredictionChecker extends BaseChecker<PredictionAnalysisDTO> {
    @Override
    protected Validator<PredictionAnalysisDTO> getValidator() {
        return new PredictionValidator<>();
    }
}
