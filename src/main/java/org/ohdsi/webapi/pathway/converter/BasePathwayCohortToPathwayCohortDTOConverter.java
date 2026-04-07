package org.ohdsi.webapi.pathway.converter;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.pathway.domain.PathwayCohort;
import org.ohdsi.webapi.pathway.dto.PathwayCohortDTO;

public abstract class BasePathwayCohortToPathwayCohortDTOConverter<T extends PathwayCohortDTO> extends BaseConversionServiceAwareConverter<PathwayCohort, T> {

    @Override
    public T convert(PathwayCohort source) {

        T result = getResultObject();
        result.setId(source.getCohortDefinition().getId());
        result.setName(StringUtils.trim(source.getName()));
        result.setDescription(source.getCohortDefinition().getDescription());
        result.setPathwayCohortId(source.getId());
        return result;
    }

    protected abstract T getResultObject();
}
