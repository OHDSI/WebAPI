package org.ohdsi.webapi.cohortcharacterization.converter;

import org.ohdsi.webapi.cohortcharacterization.dto.CcShortDTO;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortcharacterization.dto.UserDTO;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.shiro.Entities.UserEntity;

public abstract class BaseCcToCcShortDTOConverter<T extends CcShortDTO> extends BaseConversionServiceAwareConverter<CohortCharacterizationEntity, T> {
    @Override
    public T convert(final CohortCharacterizationEntity source) {
        final T dto = createResultObject();

        dto.setName(source.getName());
        dto.setId(source.getId());
        dto.setHashCode(source.getHashCode());

        dto.setCreatedAt(source.getCreatedDate());
        dto.setUpdatedAt(source.getModifiedDate());
        dto.setCreatedBy(convertUser(source.getCreatedBy()));
        dto.setUpdatedBy(convertUser(source.getModifiedBy()));

        return dto;
    }

    private UserDTO convertUser(final UserEntity userEntity) {
        if (userEntity == null) {
            return null;
        }
        final UserDTO dto = new UserDTO();
        dto.setId(userEntity.getId());
        dto.setName(userEntity.getName());
        dto.setLogin(userEntity.getLogin());
        return dto;
    }
}
