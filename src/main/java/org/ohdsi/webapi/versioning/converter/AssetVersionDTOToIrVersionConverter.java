package org.ohdsi.webapi.versioning.converter;

import org.ohdsi.webapi.versioning.domain.IrVersion;
import org.springframework.stereotype.Component;

@Component
public class AssetVersionDTOToIrVersionConverter extends BaseAssetVersionJsonDTOToAssetVersionJsonConverter<IrVersion> {
    @Override
    protected IrVersion createResultObject() {
        return new IrVersion();
    }
}
