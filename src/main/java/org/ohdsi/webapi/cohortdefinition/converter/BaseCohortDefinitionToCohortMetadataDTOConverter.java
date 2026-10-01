package org.ohdsi.webapi.cohortdefinition.converter;

import org.ohdsi.webapi.cohortdefinition.CohortDefinitionEntity;
import org.ohdsi.webapi.cohortdefinition.dto.CohortMetadataDTO;
import org.ohdsi.webapi.service.converters.BaseCommonEntityExtToDTOExtConverter;

public abstract class BaseCohortDefinitionToCohortMetadataDTOConverter<T extends CohortMetadataDTO>
        extends BaseCommonEntityExtToDTOExtConverter<CohortDefinitionEntity, T> {

    @Override
    public void doConvert(CohortDefinitionEntity def, T target) {
        target.setId(def.getId());
        target.setName(def.getName());
        target.setDescription(def.getDescription());
    }
}
