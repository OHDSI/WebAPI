package org.ohdsi.webapi.reusable.converter;

import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.reusable.domain.Reusable;
import org.ohdsi.webapi.versioning.domain.ReusableVersion;
import org.springframework.stereotype.Component;

@Component
public class ReusableToReusableVersionConverter
        extends BaseConversionServiceAwareConverter<Reusable, ReusableVersion> {
    @Override
    public ReusableVersion convert(Reusable source) {
        ReusableVersion target = new ReusableVersion();
        target.setAssetId(source.getId());
        target.setDescription(source.getDescription());
        target.setAssetJson(source.getData());

        return target;
    }
}
