package org.ohdsi.webapi.cohortdefinition.converter;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionEntity;
import org.ohdsi.webapi.cohortdefinition.dto.CohortMetadataDTO;
import org.ohdsi.webapi.service.converters.BaseCommonDTOExtToEntityExtConverter;

public abstract class BaseCohortDTOToCohortDefinitionConverter<V extends CohortMetadataDTO> extends BaseCommonDTOExtToEntityExtConverter<V, CohortDefinitionEntity> {

    @Override
    protected void doConvert(V source, CohortDefinitionEntity target) {
        target.setId(source.getId());
        target.setName(StringUtils.trim(source.getName()));
        target.setDescription(source.getDescription());
    }

    @Override
    protected CohortDefinitionEntity createResultObject() {
        return new CohortDefinitionEntity();
    }
}
