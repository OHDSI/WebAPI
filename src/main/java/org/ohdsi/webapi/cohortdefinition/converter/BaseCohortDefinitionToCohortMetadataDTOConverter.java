package org.ohdsi.webapi.cohortdefinition.converter;

import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.dto.CohortMetadataDTO;
import org.ohdsi.webapi.service.converters.BaseCommonEntityExtToDTOExtConverter;

public abstract class BaseCohortDefinitionToCohortMetadataDTOConverter<T extends CohortMetadataDTO>
        extends BaseCommonEntityExtToDTOExtConverter<CohortDefinition, T> {

    @Override
    public void doConvert(CohortDefinition def, T target) {
        target.setId(def.getId());
        target.setName(def.getName());
        target.setDescription(def.getDescription());
    }
}
