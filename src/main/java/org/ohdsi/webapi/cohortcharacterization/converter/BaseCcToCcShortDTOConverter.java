package org.ohdsi.webapi.cohortcharacterization.converter;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.cohortcharacterization.dto.CcShortDTO;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.service.converters.BaseCommonEntityExtToDTOExtConverter;

public abstract class BaseCcToCcShortDTOConverter<T extends CcShortDTO>
        extends BaseCommonEntityExtToDTOExtConverter<CohortCharacterizationEntity, T> {

    @Override
    public void doConvert(final CohortCharacterizationEntity source, T target) {
        target.setName(StringUtils.trim(source.getName()));
        target.setDescription(source.getDescription());
        target.setId(source.getId());
        target.setHashCode(source.getHashCode());
    }
}
