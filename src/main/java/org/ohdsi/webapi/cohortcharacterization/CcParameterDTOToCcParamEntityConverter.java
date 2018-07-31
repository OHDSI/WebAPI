package org.ohdsi.webapi.cohortcharacterization;

import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.springframework.stereotype.Component;

@Component
public class CcParameterDTOToCcParamEntityConverter extends BaseConversionServiceAwareConverter<CcParameterDTO, CcParamEntity> {
    @Override
    public CcParamEntity convert(final CcParameterDTO source) {
        final CcParamEntity result = new CcParamEntity();
        
        result.setName(source.getName());        
        result.setId(source.getId());
        result.setValue(source.getValue());
        
        return result;
    }
}
