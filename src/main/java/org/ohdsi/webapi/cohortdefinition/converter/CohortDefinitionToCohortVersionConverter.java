package org.ohdsi.webapi.cohortdefinition.converter;

import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetails;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.ohdsi.webapi.versioning.domain.CohortVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CohortDefinitionToCohortVersionConverter
        extends BaseConversionServiceAwareConverter<CohortDefinition, CohortVersion> {
    @Override
    public CohortVersion convert(CohortDefinition source) {
        CohortVersion target = new CohortVersion();
        target.setAssetId(source.getId());
        target.setDescription(source.getDescription());
        target.setAssetJson(source.getDetails().getExpression());

        return target;
    }
}
