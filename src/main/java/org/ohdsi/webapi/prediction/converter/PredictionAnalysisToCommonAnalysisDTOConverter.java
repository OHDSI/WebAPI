package org.ohdsi.webapi.prediction.converter;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.common.analyses.CommonAnalysisDTO;
import org.ohdsi.webapi.prediction.PredictionAnalysis;
import org.ohdsi.webapi.service.converters.BaseCommonEntityToDTOConverter;
import org.springframework.stereotype.Component;

@Component
public class PredictionAnalysisToCommonAnalysisDTOConverter <T extends CommonAnalysisDTO>
        extends BaseCommonEntityToDTOConverter<PredictionAnalysis, T> {

    @Override
    protected T createResultObject() {

        return (T) new CommonAnalysisDTO();
    }

    @Override
    public void doConvert(PredictionAnalysis source, T target) {
        target.setId(source.getId());
        target.setName(StringUtils.trim(source.getName()));
        target.setDescription(source.getDescription());
    }
}
