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
    public PredictionAnalysisDTO convert(PredictionAnalysis source) {

        PredictionAnalysisDTO result = super.convert(source);
        result.setSpecification(source.getSpecification());
        return result;
    }
}
