package org.ohdsi.webapi.pathway.converter;

import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.pathway.domain.PathwayCohort;
import org.ohdsi.webapi.pathway.dto.PathwayCohortDTO;

public abstract class BasePathwayCohortDTOToPathwayCohortConverter<T extends  PathwayCohort> extends BaseConversionServiceAwareConverter<PathwayCohortDTO, T> {

    @Override
    public T convert(PathwayCohortDTO source) {

        T result = getResultObject();
        result.setId(source.getPathwayCohortId());
        result.setName(source.getName());
        CohortDefinition cohortDefinition = new CohortDefinition();
        cohortDefinition.setId(source.getId());
        result.setCohortDefinition(cohortDefinition);
        return result;
    }

    protected abstract T getResultObject();
}
