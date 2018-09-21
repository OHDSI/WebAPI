package org.ohdsi.webapi.cohortcharacterization.converter;

import org.ohdsi.webapi.cohortcharacterization.domain.CcGenerationEntity;
import org.ohdsi.webapi.cohortcharacterization.dto.CcGenerationDTO;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.springframework.stereotype.Component;

@Component
public class CcGenerationEntityToCcGenerationDtoConverter extends BaseConversionServiceAwareConverter<CcGenerationEntity, CcGenerationDTO> {
    @Override
    public CcGenerationDTO convert(final CcGenerationEntity source) {
        final CcGenerationDTO resultObject = createResultObject();
        
        resultObject.setId(source.getId());
        resultObject.setStatus(source.getStatus());
        resultObject.setSourceKey(source.getSource().getSourceKey());
        resultObject.setHashCode(source.getHashCode());
        resultObject.setStartTime(source.getStartTime());
        resultObject.setEndTime(source.getEndTime());
        resultObject.setExitMessage(source.getExitMessage());
        
        return resultObject;
    }

    @Override
    protected CcGenerationDTO createResultObject() {
        return new CcGenerationDTO();
    }
}
