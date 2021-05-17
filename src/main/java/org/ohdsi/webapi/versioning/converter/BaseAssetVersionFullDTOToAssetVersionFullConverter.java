package org.ohdsi.webapi.versioning.converter;

import org.ohdsi.webapi.service.converters.BaseCommonDTOToEntityConverter;
import org.ohdsi.webapi.versioning.domain.AssetVersionFull;
import org.ohdsi.webapi.versioning.dto.AssetVersionFullDTO;
import org.springframework.stereotype.Component;

@Component
public abstract class BaseAssetVersionFullDTOToAssetVersionFullConverter<T extends AssetVersionFull> extends BaseCommonDTOToEntityConverter<AssetVersionFullDTO, T> {
    @Override
    protected void doConvert(AssetVersionFullDTO source, T target) {
        target.setVersion(source.getVersion());
        target.setAssetId(source.getAssetId());
        target.setId(source.getId());
        target.setName(source.getName());
        target.setArchived(source.isArchived());
        target.setAssetJson(source.getAssetJson());
    }
}
