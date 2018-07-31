package org.ohdsi.webapi.cohortcharacterization;

import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.springframework.stereotype.Component;

@Component
public class CcToCcCreateDTOConverter extends BaseConversionServiceAwareConverter<CohortCharacterizationEntity, CohortCharacterizationDTO> {

    @Override
    public CohortCharacterizationDTO convert(final CohortCharacterizationEntity source) {
        final CohortCharacterizationDTO dto = new CohortCharacterizationDTO();
        dto.setName(source.getName());
        dto.setId(source.getId());
        return dto;
    }
}
