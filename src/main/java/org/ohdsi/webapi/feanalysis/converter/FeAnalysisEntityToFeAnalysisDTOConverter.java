package org.ohdsi.webapi.feanalysis.converter;

import com.odysseusinc.arachne.commons.utils.ConverterUtils;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisCriteriaDTO;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FeAnalysisEntityToFeAnalysisDTOConverter extends BaseFeAnalysisEntityToFeAnalysisDTOConverter<FeAnalysisDTO> {
    
    @Autowired
    private ConverterUtils converterUtils;
    
    @Override
    public FeAnalysisDTO convert(final FeAnalysisEntity source) {
        final FeAnalysisDTO dto = super.convert(source);
        dto.setValue(source.getValue());
        dto.setDesign(convertDesignToJson(source));
        return dto;
    }

    @Override
    protected FeAnalysisDTO getReturnObject() {
        return new FeAnalysisDTO();
    }

    private Object convertDesignToJson(final FeAnalysisEntity source) {
        switch (source.getType()) {
            case CRITERIA_SET:
                return converterUtils.convertList(source.getDesign(), FeAnalysisCriteriaDTO.class);
            default:
                return source.getDesign();
        }
    }
}
