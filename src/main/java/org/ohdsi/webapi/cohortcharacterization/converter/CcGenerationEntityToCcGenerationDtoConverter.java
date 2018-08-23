package org.ohdsi.webapi.cohortcharacterization.converter;

import org.ohdsi.webapi.cohortcharacterization.dto.CcGenerationEntity;
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
        
        return resultObject;
    }

    @Override
    protected CcGenerationDTO createResultObject() {
        return new CcGenerationDTO();
    }
}
