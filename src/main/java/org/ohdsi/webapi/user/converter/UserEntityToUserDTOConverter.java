package org.ohdsi.webapi.user.converter;

import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.user.dto.UserDTO;
import org.springframework.stereotype.Component;

@Component
public class UserEntityToUserDTOConverter extends BaseConversionServiceAwareConverter<UserEntity, UserDTO> {

    @Override
    public UserDTO convert(UserEntity userEntity) {

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
