package org.ohdsi.webapi.feanalysis.converter;

import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisCriteriaDTO;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisCriteriaEntity;
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
