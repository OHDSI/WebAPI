package org.ohdsi.webapi.check.checker.prediction;

import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;
import org.ohdsi.webapi.check.checker.BaseChecker;
import org.ohdsi.webapi.check.checker.prediction.helper.PredictionBuilderHelper;
import org.ohdsi.webapi.prediction.dto.PredictionAnalysisDTO;
import org.springframework.stereotype.Component;

@Component
public class PredictionChecker extends BaseChecker<PredictionAnalysisDTO> {

    @PostConstruct
    public void init() {
        createValidator();
    }

    @Override
    protected List<ValidatorGroupBuilder<PredictionAnalysisDTO, ?>> getGroupBuilder() {

        return Arrays.asList(
                PredictionBuilderHelper.prepareAnalysisExpressionBuilder()
        );
    }
}
