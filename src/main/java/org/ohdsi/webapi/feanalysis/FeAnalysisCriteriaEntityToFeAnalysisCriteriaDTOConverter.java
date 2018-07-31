package org.ohdsi.webapi.feanalysis;

import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.springframework.stereotype.Component;

@Component
public class FeAnalysisCriteriaEntityToFeAnalysisCriteriaDTOConverter extends BaseConversionServiceAwareConverter<FeAnalysisCriteriaEntity, FeAnalysisCriteriaDTO> {
    @Override
    public FeAnalysisCriteriaDTO convert(final FeAnalysisCriteriaEntity source) {
        final FeAnalysisCriteriaDTO dto = new FeAnalysisCriteriaDTO();
        dto.setName(source.getName());
        dto.setExpression(source.getExpression());
        return dto;
    }
}
