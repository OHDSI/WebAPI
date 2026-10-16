package org.ohdsi.webapi.feanalysis.converter;

import org.ohdsi.webapi.cohortcharacterization.domain.CcFeAnalysisEntity;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisShortDTO;
import org.springframework.stereotype.Component;

@Component
public class CcFeAnalysisEntityToFeAnalysisShortDTOConverter extends BaseConversionServiceAwareConverter<CcFeAnalysisEntity, FeAnalysisShortDTO> {

    @Override
    public FeAnalysisShortDTO convert(CcFeAnalysisEntity source) {
        FeAnalysisShortDTO dto = conversionService.convert(source.getFeatureAnalysis(), FeAnalysisShortDTO.class);
        dto.setIncludeAnnual(source.getIncludeAnnual());
        dto.setIncludeTemporal(source.getIncludeTemporal());
        return dto;
    }
}
