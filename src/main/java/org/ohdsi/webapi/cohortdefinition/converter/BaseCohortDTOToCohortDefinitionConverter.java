package org.ohdsi.webapi.cohortdefinition.converter;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.dto.CohortMetadataDTO;
import org.ohdsi.webapi.service.converters.BaseCommonDTOExtToEntityExtConverter;

public abstract class BaseCohortDTOToCohortDefinitionConverter<V extends CohortMetadataDTO> extends BaseCommonDTOExtToEntityExtConverter<V, CohortDefinition> {

    @Override
    protected void doConvert(V source, CohortDefinition target) {
        target.setId(source.getId());
        target.setName(StringUtils.trim(source.getName()));
        target.setDescription(source.getDescription());
    }

    @Override
    protected CohortDefinition createResultObject() {
        return new CohortDefinition();
    }
}
