package org.ohdsi.webapi.versioning.converter;

import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.versioning.domain.AssetVersion;
import org.ohdsi.webapi.versioning.dto.AssetVersionDTO;
import org.springframework.stereotype.Component;

@Component
public abstract class BaseAssetVersionDTOToAssetVersionConverter<T extends AssetVersion> extends BaseConversionServiceAwareConverter<AssetVersionDTO, T> {
    @Override
    public T convert(AssetVersionDTO s) {
        T target = createResultObject(s);
        doConvert(s, target);
        return target;
    }

    protected void doConvert(AssetVersionDTO source, T target) {
        target.setVersion(source.getVersion());
        target.setAssetId(source.getAssetId());
        target.setId(source.getId());
        target.setComment(source.getComment());
        target.setArchived(source.isArchived());
        target.setAssetJson(source.getAssetJson());
    }
}
