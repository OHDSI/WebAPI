package org.ohdsi.webapi.versioning.converter;

import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.user.dto.UserDTO;
import org.ohdsi.webapi.versioning.domain.AssetVersion;
import org.ohdsi.webapi.versioning.domain.AssetVersionBase;
import org.ohdsi.webapi.versioning.dto.AssetVersionBaseDTO;
import org.ohdsi.webapi.versioning.dto.AssetVersionDTO;
import org.springframework.stereotype.Component;

@Component
public class AssetVersionBaseToAssetVersionDTOConverter extends BaseConversionServiceAwareConverter<AssetVersionBase, AssetVersionBaseDTO> {
    @Override
    public AssetVersionDTO convert(AssetVersionBase source) {
        AssetVersionDTO target = new AssetVersionDTO();

        target.setComment(source.getComment());
        target.setAssetId(source.getAssetId());
        target.setId(source.getId());
        target.setVersion(source.getVersion());
        target.setArchived(source.isArchived());
        target.setCreatedBy(conversionService.convert(source.getCreatedBy(), UserDTO.class));
        target.setCreatedDate(source.getCreatedDate());

        return target;
    }
}
