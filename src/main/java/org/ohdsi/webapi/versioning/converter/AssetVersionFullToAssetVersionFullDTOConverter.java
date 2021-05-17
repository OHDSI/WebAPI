package org.ohdsi.webapi.versioning.converter;

import org.ohdsi.webapi.service.converters.BaseCommonEntityToDTOConverter;
import org.ohdsi.webapi.versioning.domain.AssetVersionFull;
import org.ohdsi.webapi.versioning.dto.AssetVersionFullDTO;
import org.springframework.stereotype.Component;

@Component
public class AssetVersionFullToAssetVersionFullDTOConverter extends
        BaseCommonEntityToDTOConverter<AssetVersionFull, AssetVersionFullDTO> {
    @Override
    protected void doConvert(AssetVersionFull source, AssetVersionFullDTO target) {
        target.setName(source.getName());
        target.setAssetId(source.getAssetId());
        target.setId(source.getId());
        target.setVersion(source.getVersion());
        target.setArchived(source.isArchived());
        target.setAssetJson(source.getAssetJson());
    }

    @Override
    protected AssetVersionFullDTO createResultObject() {
        return new AssetVersionFullDTO();
    }
}
