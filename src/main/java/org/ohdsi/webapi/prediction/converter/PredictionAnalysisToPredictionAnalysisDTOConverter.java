package org.ohdsi.webapi.prediction.converter;

import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.prediction.PredictionAnalysis;
import org.ohdsi.webapi.prediction.dto.PredictionAnalysisDTO;
import org.ohdsi.webapi.user.dto.UserDTO;
import org.springframework.stereotype.Component;

@Component
public class PredictionAnalysisToPredictionAnalysisDTOConverter extends BaseConversionServiceAwareConverter<PredictionAnalysis, PredictionAnalysisDTO> {

    @Override
    public PredictionAnalysisDTO convert(PredictionAnalysis source) {

        PredictionAnalysisDTO result = new PredictionAnalysisDTO();
        result.setId(source.getId());
        result.setName(source.getName());
        result.setDescription(source.getDescription());
        result.setSpecification(source.getSpecification());
        result.setCreatedBy(conversionService.convert(source.getCreatedBy(), UserDTO.class));
        result.setCreatedDate(source.getCreatedDate());
        result.setModifiedBy(conversionService.convert(source.getModifiedBy(), UserDTO.class));
        result.setModifiedDate(source.getModifiedDate());
        return result;
    }
}
