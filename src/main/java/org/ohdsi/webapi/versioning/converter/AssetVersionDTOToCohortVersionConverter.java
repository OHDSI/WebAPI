package org.ohdsi.webapi.versioning.converter;

import org.ohdsi.webapi.versioning.domain.CohortVersion;
import org.springframework.stereotype.Component;

@Component
public class AssetVersionDTOToCohortVersionConverter extends BaseAssetVersionJsonDTOToAssetVersionJsonConverter<CohortVersion> {
    @Override
    protected CohortVersion createResultObject() {
        return new CohortVersion();
    }
}
