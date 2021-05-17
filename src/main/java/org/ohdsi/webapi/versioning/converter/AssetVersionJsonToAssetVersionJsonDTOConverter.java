package org.ohdsi.webapi.versioning.converter;

import org.ohdsi.webapi.service.converters.BaseCommonEntityToDTOConverter;
import org.ohdsi.webapi.versioning.domain.AssetVersionFull;
import org.ohdsi.webapi.versioning.dto.AssetVersionJsonDTO;
import org.springframework.stereotype.Component;

@Component
public class AssetVersionJsonToAssetVersionJsonDTOConverter extends
        BaseCommonEntityToDTOConverter<AssetVersionFull, AssetVersionJsonDTO> {
    @Override
    protected void doConvert(AssetVersionFull source, AssetVersionJsonDTO target) {
        target.setName(source.getName());
        target.setAssetId(source.getAssetId());
        target.setId(source.getId());
        target.setVersion(source.getVersion());
        target.setArchived(source.isArchived());
        target.setAssetJson(source.getAssetJson());
    }

    @Override
    protected AssetVersionJsonDTO createResultObject() {
        return new AssetVersionJsonDTO();
    }
}
