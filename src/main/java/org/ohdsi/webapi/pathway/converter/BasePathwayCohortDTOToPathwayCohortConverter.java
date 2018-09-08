package org.ohdsi.webapi.pathway.converter;

import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.pathway.domain.PathwayCohort;
import org.ohdsi.webapi.pathway.dto.PathwayCohortDTO;
import org.springframework.stereotype.Component;

public abstract class BasePathwayCohortDTOToPathwayCohortConverter<T extends  PathwayCohort> extends BaseConversionServiceAwareConverter<PathwayCohortDTO, T> {

    @Override
    public T convert(PathwayCohortDTO source) {

        T result = getResultObject();
        result.setId(source.getId());
        result.setName(source.getName());
        CohortDefinition cohortDefinition = new CohortDefinition();
        cohortDefinition.setId(source.getCohortDefinitionId());
        result.setCohortDefinition(cohortDefinition);
        return result;
    }

    protected abstract T getResultObject();
}
