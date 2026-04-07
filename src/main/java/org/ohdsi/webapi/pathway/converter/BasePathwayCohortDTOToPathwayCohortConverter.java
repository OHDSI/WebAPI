package org.ohdsi.webapi.pathway.converter;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionEntity;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.pathway.domain.PathwayCohort;
import org.ohdsi.webapi.pathway.dto.PathwayCohortDTO;
import org.springframework.core.convert.ConversionService;

public abstract class BasePathwayCohortDTOToPathwayCohortConverter<S extends PathwayCohortDTO, R extends PathwayCohort> extends BaseConversionServiceAwareConverter<S, R> {

    private ConversionService conversionService;

    public BasePathwayCohortDTOToPathwayCohortConverter(ConversionService conversionService) {

        this.conversionService = conversionService;
    }

    @Override
    public R convert(S source) {

        R result = getResultObject();
        result.setId(source.getPathwayCohortId());
        result.setName(StringUtils.trim(source.getName()));
        result.setCohortDefinition(convertCohort(source));
        return result;
    }

    protected abstract R getResultObject();

    protected CohortDefinitionEntity convertCohort(S source) {

        return conversionService.convert(source, CohortDefinitionEntity.class);
    }
}
