package org.ohdsi.webapi.cohortcharacterization.converter;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.cohortcharacterization.dto.CcShortDTO;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.service.converters.BaseCommonEntityToDTOConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;

public abstract class BaseCcToCcShortDTOConverter<T extends CcShortDTO>
        extends BaseCommonEntityToDTOConverter<CohortCharacterizationEntity, T> {

    @Autowired
    protected ConversionService conversionService;

    @Override
    public void doConvert(final CohortCharacterizationEntity source, T target) {
        target.setName(StringUtils.trim(source.getName()));
        target.setId(source.getId());
        target.setHashCode(source.getHashCode());
    }
}
