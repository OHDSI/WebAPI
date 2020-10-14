package org.ohdsi.webapi.prediction.converter;

import org.ohdsi.webapi.prediction.PredictionAnalysis;
import org.ohdsi.webapi.prediction.dto.PredictionAnalysisDTO;
import org.springframework.stereotype.Component;

@Component
public class PredictionAnalysisToPredictionAnalysisDTOConverter extends PredictionAnalysisToCommonAnalysisDTOConverter<PredictionAnalysisDTO> {

    @Override
    protected PredictionAnalysisDTO createResultObject() {

        return new PredictionAnalysisDTO();
    }

    @Override
    public void doConvert(PredictionAnalysis source, PredictionAnalysisDTO target) {
        super.doConvert(source, target);
        target.setSpecification(source.getSpecification());
    }
}
