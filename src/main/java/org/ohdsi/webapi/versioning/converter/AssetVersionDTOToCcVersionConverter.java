package org.ohdsi.webapi.versioning.converter;

import org.ohdsi.webapi.versioning.domain.CohortCharacterizationVersion;
import org.springframework.stereotype.Component;

@Component
public class AssetVersionDTOToCcVersionConverter extends BaseAssetVersionFullDTOToAssetVersionFullConverter<CohortCharacterizationVersion> {
    @Override
    protected CohortCharacterizationVersion createResultObject() {
        return new CohortCharacterizationVersion();
    }
}
