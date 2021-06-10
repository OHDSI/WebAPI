package org.ohdsi.webapi.security.converter;

import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.security.dto.RoleDTO;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.springframework.stereotype.Component;

@Component
public class RoleEntityToRoleDTOConverter extends BaseConversionServiceAwareConverter<RoleEntity, RoleDTO> {

    @Override
    public RoleDTO convert(RoleEntity roleEntity) {

        return new RoleDTO(roleEntity.getId(), roleEntity.getName());
    }
}
