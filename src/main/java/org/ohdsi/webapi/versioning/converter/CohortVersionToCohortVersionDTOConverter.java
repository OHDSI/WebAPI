package org.ohdsi.webapi.versioning.converter;

import org.ohdsi.webapi.versioning.domain.CohortVersion;
import org.ohdsi.webapi.versioning.dto.CohortVersionDTO;
import org.springframework.stereotype.Component;

@Component
public class CohortVersionToCohortVersionDTOConverter extends AbstractVersionToVersionDTOConverter<CohortVersion, CohortVersionDTO> {
    @Override
    protected void doConvert(CohortVersion source, CohortVersionDTO target) {
        super.doConvert(source, target);
        target.setDescription(source.getDescription());
    }

    @Override
    protected CohortVersionDTO createResultObject() {
        return new CohortVersionDTO();
    }
}
