package org.ohdsi.webapi.cohortcharacterization.converter;

import org.ohdsi.webapi.cohortcharacterization.domain.CcParamEntity;
import org.ohdsi.webapi.cohortcharacterization.dto.CcParameterDTO;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.springframework.stereotype.Component;

@Component
public class CcParamEntityToCcParameterDTOConverter extends BaseConversionServiceAwareConverter<CcParamEntity, CcParameterDTO> {
    @Override
    public CcParameterDTO convert(final CcParamEntity source) {
        final CcParameterDTO dto = new CcParameterDTO();
        dto.setValue(source.getValue());
        dto.setName(source.getName());
        dto.setId(source.getId());
        return dto;
    }
}
