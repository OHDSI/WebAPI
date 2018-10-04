package org.ohdsi.webapi.cohortcharacterization.converter;

import org.ohdsi.webapi.cohortcharacterization.dto.CcShortDTO;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.user.dto.UserDTO;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;

public abstract class BaseCcToCcShortDTOConverter<T extends CcShortDTO> extends BaseConversionServiceAwareConverter<CohortCharacterizationEntity, T> {

    @Autowired
    protected ConversionService conversionService;

    @Override
    public T convert(final CohortCharacterizationEntity source) {
        final T dto = createResultObject();

        dto.setName(source.getName());
        dto.setId(source.getId());
        dto.setHashCode(source.getHashCode());

        dto.setCreatedBy(conversionService.convert(source.getCreatedBy(), UserDTO.class));
        dto.setUpdatedBy(conversionService.convert(source.getModifiedBy(), UserDTO.class));
        dto.setCreatedAt(source.getCreatedDate());
        dto.setUpdatedAt(source.getModifiedDate());

        return dto;
    }
}
