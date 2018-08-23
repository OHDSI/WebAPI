package org.ohdsi.webapi.cohortcharacterization.converter;

import org.ohdsi.webapi.cohortcharacterization.dto.CcCreateDTO;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.springframework.stereotype.Component;

@Component
public class CcCreateDTOToCcConverter<T extends CcCreateDTO> extends BaseConversionServiceAwareConverter<T, CohortCharacterizationEntity> {

    @Override
    public CohortCharacterizationEntity convert(final T source) {
        final CohortCharacterizationEntity entity = new CohortCharacterizationEntity();
        entity.setName(source.getName());
        return entity;
    }
}
