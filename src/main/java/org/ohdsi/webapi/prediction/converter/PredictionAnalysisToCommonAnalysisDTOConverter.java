package org.ohdsi.webapi.prediction.converter;

import org.ohdsi.webapi.common.analyses.CommonAnalysisDTO;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.prediction.PredictionAnalysis;
import org.ohdsi.webapi.user.dto.UserDTO;
import org.springframework.stereotype.Component;

@Component
public class PredictionAnalysisToCommonAnalysisDTOConverter extends BaseConversionServiceAwareConverter<PredictionAnalysis, CommonAnalysisDTO> {

    @Override
    public CommonAnalysisDTO convert(PredictionAnalysis source) {

        CommonAnalysisDTO result = new CommonAnalysisDTO();
        result.setId(source.getId());
        result.setName(source.getName());
        result.setDescription(source.getDescription());
        result.setCreatedBy(conversionService.convert(source.getCreatedBy(), UserDTO.class));
        result.setCreatedDate(source.getCreatedDate());
        result.setModifiedBy(conversionService.convert(source.getModifiedBy(), UserDTO.class));
        result.setModifiedDate(source.getModifiedDate());
        return result;
    }
}
