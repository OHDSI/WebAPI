package org.ohdsi.webapi.versioning.converter;

import org.ohdsi.webapi.user.dto.UserDTO;
import org.ohdsi.webapi.versioning.domain.Version;
import org.ohdsi.webapi.versioning.dto.VersionDTO;
import org.springframework.stereotype.Component;

@Component
public class VersionToVersionDTOConverter extends AbstractVersionToVersionDTOConverter<Version, VersionDTO> {
    @Override
    protected VersionDTO createResultObject() {
        return new VersionDTO();
    }
}
