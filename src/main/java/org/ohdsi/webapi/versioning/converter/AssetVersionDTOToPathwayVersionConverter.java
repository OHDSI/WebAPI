package org.ohdsi.webapi.versioning.converter;

import org.ohdsi.webapi.versioning.domain.PathwayVersion;
import org.springframework.stereotype.Component;

@Component
public class AssetVersionDTOToPathwayVersionConverter extends BaseAssetVersionDTOToAssetVersionConverter<PathwayVersion> {
    @Override
    protected PathwayVersion createResultObject() {
        return new PathwayVersion();
    }
}
