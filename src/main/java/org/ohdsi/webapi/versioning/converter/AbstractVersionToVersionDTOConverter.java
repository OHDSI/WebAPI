package org.ohdsi.webapi.versioning.converter;

import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.user.dto.UserDTO;
import org.ohdsi.webapi.versioning.domain.CohortVersion;
import org.ohdsi.webapi.versioning.domain.Version;
import org.ohdsi.webapi.versioning.dto.CohortVersionDTO;
import org.ohdsi.webapi.versioning.dto.VersionDTO;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractVersionToVersionDTOConverter<S extends Version, T extends VersionDTO> extends BaseConversionServiceAwareConverter<S, T> {
    protected void doConvert(S source, T target) {
        target.setComment(source.getComment());
        target.setAssetId(source.getAssetId());
        target.setId(source.getId());
        target.setVersion(source.getVersion());
        target.setArchived(source.isArchived());
        target.setAssetJson(source.getAssetJson());
        target.setCreatedBy(conversionService.convert(source.getCreatedBy(), UserDTO.class));
        target.setCreatedDate(source.getCreatedDate());
    }

    @Override
    public T convert(S s) {
        T target = createResultObject(s);
        doConvert(s, target);
        return target;
    }
}
