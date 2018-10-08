package org.ohdsi.webapi.cohortdefinition.converter;

import org.ohdsi.analysis.CohortMetadata;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;

public class BaseCohortDTOToCohortDefinitionConverter<T extends CohortMetadata> extends BaseConversionServiceAwareConverter<T, CohortDefinition> {

    @Override
    public CohortDefinition convert(T source) {

        CohortDefinition cohortDefinition = new CohortDefinition();

        cohortDefinition.setId(source.getId());
        cohortDefinition.setName(source.getName());
        cohortDefinition.setDescription(source.getDescription());

        return cohortDefinition;
    }
}
