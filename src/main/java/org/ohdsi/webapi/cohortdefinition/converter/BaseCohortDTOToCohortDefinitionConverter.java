package org.ohdsi.webapi.cohortdefinition.converter;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.dto.CohortMetadataDTO;
import org.ohdsi.webapi.service.converters.BaseCommonDTOExtToEntityExtConverter;

public class BaseCohortDTOToCohortDefinitionConverter<T extends CohortDefinition> extends BaseCommonDTOExtToEntityExtConverter<CohortMetadataDTO, T> {

    @Override
    protected void doConvert(CohortMetadataDTO source, T target) {
        target.setId(source.getId());
        target.setName(StringUtils.trim(source.getName()));
        target.setDescription(source.getDescription());
    }
}
