package org.ohdsi.webapi.versioning.converter;

import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.user.dto.UserDTO;
import org.ohdsi.webapi.versioning.domain.AssetVersion;
import org.ohdsi.webapi.versioning.dto.AssetVersionDTO;
import org.springframework.stereotype.Component;

@Component
public class AssetVersionToAssetVersionDTOConverter<S extends AssetVersion, T extends AssetVersionDTO> extends BaseConversionServiceAwareConverter<AssetVersion, AssetVersionDTO> {
    @Override
    public AssetVersionDTO convert(AssetVersion source) {
        AssetVersionDTO target = new AssetVersionDTO();

        target.setName(source.getName());
        target.setAssetId(source.getAssetId());
        target.setId(source.getId());
        target.setVersion(source.getVersion());
        target.setArchived(source.isArchived());
        target.setAssetJson(source.getAssetJson());
        target.setCreatedBy(conversionService.convert(source.getCreatedBy(), UserDTO.class));
        target.setCreatedDate(source.getCreatedDate());

        return target;
    }
}
