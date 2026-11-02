package org.ohdsi.webapi.versioning.converter;

import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.security.authz.User;
import org.ohdsi.webapi.versioning.domain.Version;
import org.ohdsi.webapi.versioning.dto.VersionDTO;
import org.springframework.stereotype.Component;

@Component
public class VersionToVersionDTOConverter extends BaseConversionServiceAwareConverter<Version, VersionDTO> {
    @Override
    public VersionDTO convert(Version source) {
        VersionDTO target = new VersionDTO();

        target.setComment(source.getComment());
        target.setAssetId(source.getAssetId());
        target.setVersion(source.getVersion());
        target.setArchived(source.isArchived());
        target.setCreatedBy(User.fromEntity(source.getCreatedBy()));
        target.setCreatedDate(source.getCreatedDate());

        return target;
    }
}
