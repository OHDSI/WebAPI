package org.ohdsi.webapi.feanalysis.converter;

import com.odysseusinc.arachne.commons.utils.ConverterUtils;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisCriteriaDTO;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisDTO;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FeAnalysisEntityToFeAnalysisDTOConverter extends BaseConversionServiceAwareConverter<FeAnalysisEntity, FeAnalysisDTO> {
    
    @Autowired
    private ConverterUtils converterUtils;
    
    @Override
    public FeAnalysisDTO convert(final FeAnalysisEntity source) {
        final FeAnalysisDTO dto = new FeAnalysisDTO();
        dto.setType(source.getType());
        dto.setName(source.getName());
        dto.setId(source.getId());
        dto.setDescription(source.getDescr());
        dto.setValue(source.getValue());
        dto.setDomain(source.getDomain());
        dto.setDesign(convertDesignToJson(source));
        return dto;
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
